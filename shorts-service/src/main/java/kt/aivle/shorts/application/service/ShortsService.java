package kt.aivle.shorts.application.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.shorts.adapter.in.web.dto.ScenarioResponse;
import kt.aivle.shorts.application.port.in.ShortsUseCase;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.out.AiWebClientPort;
import kt.aivle.shorts.application.port.out.ImageStoragePort;
import kt.aivle.shorts.application.port.out.StoreInfoQueryPort;
import kt.aivle.shorts.application.service.dto.ScenarioDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static kt.aivle.shorts.exception.ShortsErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortsService implements ShortsUseCase {

    private final StoreInfoQueryPort storeInfoQueryPort;
    private final AiWebClientPort aiWebClientPort;
    private final ImageStoragePort imageStoragePort;

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
                                .flatMap(urlList -> {
                                    ScenarioDto scenarioDto = ScenarioDto.from(command, storeInfo, urlList);
                                    return aiWebClientPort.callScenario(scenarioDto)
                                            .onErrorMap(e -> new BusinessException(AI_WEB_CLIENT_ERROR, e.getMessage()));
                                })
                );
    }
}