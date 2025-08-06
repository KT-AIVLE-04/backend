package kt.aivle.shorts.application.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.shorts.adapter.in.web.dto.ScenarioResponse;
import kt.aivle.shorts.application.port.in.ShortsUseCase;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.out.StoreInfoQueryPort;
import kt.aivle.shorts.application.service.dto.ScenarioDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

import static kt.aivle.shorts.exception.ShortsErrorCode.NOT_GET_STORE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortsService implements ShortsUseCase {

    private final StoreInfoQueryPort storeInfoQueryPort;
//    private final ScenarioCommandMapper scenarioCommandMapper;
//    private final AiWebClientPort aiWebClientPort; // FastAPI 연동 추상화

    @Override
    public Mono<List<ScenarioResponse>> createScenario(CreateScenarioCommand command) {
        return storeInfoQueryPort.getStoreInfo(command.storeId(), command.userId())
                .flatMap(storeInfo ->
                                command.images().collectList().flatMap(imageList -> {
                                    ScenarioDto scenarioDto = ScenarioDto.from(command, storeInfo, imageList);
                                    ScenarioResponse scenarioResponse = null;
//                            return aiWebClientPort.callScenario(aiDto);
                                    return Mono.just(List.of(scenarioResponse));
                                })
                )
                .onErrorMap(e -> new BusinessException(NOT_GET_STORE, e));
    }
}
