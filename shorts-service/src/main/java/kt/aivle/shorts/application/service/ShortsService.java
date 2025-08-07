package kt.aivle.shorts.application.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.shorts.adapter.in.web.dto.ScenarioResponse;
import kt.aivle.shorts.adapter.out.event.CreateContentRequestEvent;
import kt.aivle.shorts.adapter.out.s3.UploadedImageInfo;
import kt.aivle.shorts.application.port.in.ShortsUseCase;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.out.AiWebClientPort;
import kt.aivle.shorts.application.port.out.ContentsEventPort;
import kt.aivle.shorts.application.port.out.ImageStoragePort;
import kt.aivle.shorts.application.port.out.StoreInfoQueryPort;
import kt.aivle.shorts.application.service.dto.ScenarioDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

import static kt.aivle.shorts.exception.ShortsErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortsService implements ShortsUseCase {

    private final StoreInfoQueryPort storeInfoQueryPort;
    private final AiWebClientPort aiWebClientPort;
    private final ImageStoragePort imageStoragePort;
    private final ContentsEventPort contentsEventPort;

    @Override
    public Mono<ScenarioResponse> createScenario(CreateScenarioCommand command) {
        return storeInfoQueryPort.getStoreInfo(command.storeId(), command.userId())
                .onErrorMap(e -> new BusinessException(NOT_GET_STORE, e.getMessage()))
                .flatMap(storeInfo ->
                        command.images().collectList()
                                .flatMap(imageList ->
                                        imageStoragePort.uploadImages(imageList)
                                                .onErrorMap(e -> new BusinessException(IMAGE_UPLOAD_ERROR, e.getMessage()))
                                )
                                .flatMap(uploadedImageInfos -> {
                                    List<String> urlList = uploadedImageInfos.stream()
                                            .map(UploadedImageInfo::url)
                                            .toList();

                                    ScenarioDto scenarioDto = ScenarioDto.from(command, storeInfo, urlList);

                                    return aiWebClientPort.callScenario(scenarioDto)
                                            .onErrorResume(aiError -> imageStoragePort.deleteImages(uploadedImageInfos)
                                                    .then(Mono.error(new BusinessException(AI_WEB_CLIENT_ERROR, aiError.getMessage()))))
                                            .flatMap(scenarioResponse -> contentsEventPort.publish(new CreateContentRequestEvent(command.storeId(), uploadedImageInfos)
                                                    )
                                                    .thenReturn(scenarioResponse)
                                                    .onErrorResume(eventError -> imageStoragePort.deleteImages(uploadedImageInfos)
                                                            .then(Mono.error(new BusinessException(CONTENTS_EVENT_ERROR, eventError.getMessage())))));

                                })
                );
    }
}