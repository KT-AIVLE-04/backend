package kt.aivle.shorts.application.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.shorts.application.port.in.ShortsUseCase;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.command.CreateShortsCommand;
import kt.aivle.shorts.application.port.in.command.SaveShortsCommand;
import kt.aivle.shorts.application.port.in.dto.ScenarioDTO;
import kt.aivle.shorts.application.port.out.ai.shorts.AiShortsPort;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateShortsRequest;
import kt.aivle.shorts.application.port.out.event.contents.ContentServicePort;
import kt.aivle.shorts.application.port.out.event.contents.CreateContentRequest;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoResponse;
import kt.aivle.shorts.application.port.out.event.store.StoreServicePort;
import kt.aivle.shorts.application.port.out.job.JobStore;
import kt.aivle.shorts.application.port.out.s3.MediaStoragePort;
import kt.aivle.shorts.application.port.out.s3.UploadedObjectResponse;
import kt.aivle.shorts.application.service.mapper.toDtoMapper;
import kt.aivle.shorts.application.service.mapper.toRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static kt.aivle.shorts.exception.ShortsErrorCode.*;

@Service
@RequiredArgsConstructor
public class ShortsService implements ShortsUseCase {

    private final AiShortsPort aiShortsPort;
    private final StoreServicePort storeServicePort;
    private final ContentServicePort contentServicePort;
    private final MediaStoragePort mediaStoragePort;
    private final JobStore jobStore;
    private final JobOrchestrator orchestrator;
    private final toRequestMapper toRequestMapper;
    private final toDtoMapper toDtoMapper;

    @Override
    public Mono<ScenarioDTO> createScenario(CreateScenarioCommand command) {
        return getStoreInfo(command.userId(), command.storeId())
                .map(store -> toRequestMapper.toGenerateScenarioRequest(command, store))
                .flatMap(req -> aiShortsPort.generateScenario(req)
                        .onErrorMap(e -> new BusinessException(AI_WEB_CLIENT_ERROR, e.getMessage())))
                .map(toDtoMapper::toScenarioDTO);
    }

    @Async("shortsExecutor")
    @Override
    public void startCreateShorts(CreateShortsCommand command) {
        final String jobId = command.sessionId();
        jobStore.init(jobId);

        uploadTempImages(command.images())
                .map(list -> list.stream().map(UploadedObjectResponse::presignedUrl).toList())
                .map(urls -> new GenerateShortsRequest(
                        command.sessionId(), command.title(), command.content(),
                        command.adDuration(), urls))
                .flatMap(aiShortsPort::generateShorts) // FastAPI 완료 시 key 반환
                .timeout(Duration.ofMinutes(45))
                .doOnSubscribe(s -> {
                    jobStore.start(jobId);
                    orchestrator.start(jobId); // 가짜 진행률 0→95%
                })
                .doOnSuccess(res -> jobStore.success(jobId, res.key()))  // 100%
                .doOnError(e -> jobStore.fail(jobId, e.getMessage()))
                .subscribe();
    }

//    @Override
//    public Mono<ShortsDTO> createShorts(CreateShortsCommand command) {
//        return uploadTempImages(command.images()) // Mono<List<UploadedObjectResponse>>
//                .flatMap(uploaded -> {
//                    List<String> presignedUrls = uploaded.stream()
//                            .map(UploadedObjectResponse::presignedUrl)
//                            .toList();
//
//                    GenerateShortsRequest req =
//                            toRequestMapper.toGenerateShortsRequest(command, presignedUrls);
//
//                    return aiShortsPort.generateShorts(req)
//                            .onErrorMap(e -> new BusinessException(AI_WEB_CLIENT_ERROR, e.getMessage()))
//                            .flatMap(res -> mediaStoragePort.getPresignedUrl(res.key())
//                                    .onErrorMap(e -> new BusinessException(S3_ERROR, "preSigned URL 발급 실패 " + e.getMessage()))
//                                    .map(videoUrl -> toDtoMapper.toShortsDTO(videoUrl, res))
//                            );
//                });
//    }

    @Override
    public Mono<Void> saveShorts(SaveShortsCommand command) {
        CreateContentRequest request = toRequestMapper.toCreateContentRequest(command);
        return contentServicePort.createContent(request).onErrorMap(e -> new BusinessException(CONTENTS_EVENT_ERROR, e.getMessage()));
    }

    private Mono<StoreInfoResponse> getStoreInfo(Long userId, Long storeId) {
        return storeServicePort
                .getStoreInfo(toRequestMapper.toStoreInfoRequest(userId, storeId))
                .onErrorMap(e -> new BusinessException(NOT_GET_STORE, e.getMessage()));
    }

    private Mono<List<UploadedObjectResponse>> uploadTempImages(Flux<FilePart> images) {
        return images
                .flatMap(mediaStoragePort::uploadTempImage)
                .collectList()
                .onErrorMap(e -> new BusinessException(IMAGE_UPLOAD_ERROR, e.getMessage()));
    }
}