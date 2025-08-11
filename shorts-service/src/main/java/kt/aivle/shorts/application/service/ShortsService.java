package kt.aivle.shorts.application.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.shorts.application.port.in.ShortsUseCase;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.dto.ScenarioDTO;
import kt.aivle.shorts.application.port.out.ai.shorts.AiShortsPort;
import kt.aivle.shorts.application.port.out.ai.shorts.GenerateScenarioRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.GenerateScenarioResponse;
import kt.aivle.shorts.application.port.out.event.contents.ContentServicePort;
import kt.aivle.shorts.application.port.out.event.contents.CreateContentRequest;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoResponse;
import kt.aivle.shorts.application.port.out.event.store.StoreServicePort;
import kt.aivle.shorts.application.port.out.s3.DeleteImageRequest;
import kt.aivle.shorts.application.port.out.s3.ImageStoragePort;
import kt.aivle.shorts.application.port.out.s3.UploadImageResponse;
import kt.aivle.shorts.application.service.mapper.ServiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static kt.aivle.shorts.exception.ShortsErrorCode.*;

@Service
@RequiredArgsConstructor
public class ShortsService implements ShortsUseCase {

    private final StoreServicePort storeServicePort;
    private final ContentServicePort contentServicePort;
    private final AiShortsPort aiShortsPort;
    private final ImageStoragePort imageStoragePort;
    private final ServiceMapper mapper;

    @Override
    public Mono<ScenarioDTO> createScenario(CreateScenarioCommand command) {
        String correlationId = UUID.randomUUID().toString();

        return Mono.zip(
                        getStoreInfo(command, correlationId),
                        uploadImages(command)
                )
                .flatMap(tuple ->
                        handleAiAndContent(command, tuple.getT1(), tuple.getT2())
                )
                .map(mapper::toScenarioDTO);
    }

    private Mono<StoreInfoResponse> getStoreInfo(CreateScenarioCommand command, String correlationId) {
        return storeServicePort
                .getStoreInfo(mapper.toStoreInfoResult(command, correlationId))
                .onErrorMap(e -> new BusinessException(NOT_GET_STORE, e.getMessage()));
    }

    private Mono<List<UploadImageResponse>> uploadImages(CreateScenarioCommand command) {
        return command.images()
                .collectList()
                .flatMap(imageStoragePort::uploadImages)
                .onErrorMap(e -> new BusinessException(IMAGE_UPLOAD_ERROR, e.getMessage()));
    }

    private Mono<GenerateScenarioResponse> handleAiAndContent(
            CreateScenarioCommand command,
            StoreInfoResponse store,
            List<UploadImageResponse> uploaded
    ) {
        List<DeleteImageRequest> deleteRequests = uploaded.stream()
                .map(img -> new DeleteImageRequest(img.s3Key()))
                .toList();

        List<String> urls = uploaded.stream()
                .map(UploadImageResponse::url)
                .toList();

        GenerateScenarioRequest outReq = mapper.toGenerateScenarioRequest(command, store, urls);

        return aiShortsPort.generateScenario(outReq)
                .onErrorResume(aiErr ->
                        imageStoragePort.deleteImages(deleteRequests)
                                .then(Mono.error(new BusinessException(AI_WEB_CLIENT_ERROR, aiErr.getMessage())))
                )
                .flatMap(outRes -> {
                    List<CreateContentRequest.ImageItem> items = uploaded.stream()
                            .map(u -> new CreateContentRequest.ImageItem(
                                    u.url(),
                                    u.s3Key(),
                                    u.originalName(),
                                    u.contentType()
                            ))
                            .toList();

                    CreateContentRequest contentsReq =
                            mapper.toCreateContentRequestMessage(command.storeId(), items);

                    return contentServicePort.createContent(contentsReq)
                            .thenReturn(outRes)
                            .onErrorResume(evErr ->
                                    imageStoragePort.deleteImages(deleteRequests)
                                            .then(Mono.error(new BusinessException(CONTENTS_EVENT_ERROR, evErr.getMessage())))
                            );
                });
    }
}