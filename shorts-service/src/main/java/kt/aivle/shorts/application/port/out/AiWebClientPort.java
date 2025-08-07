package kt.aivle.shorts.application.port.out;

import kt.aivle.shorts.adapter.in.web.dto.ScenarioResponse;
import kt.aivle.shorts.application.service.dto.ScenarioDto;
import reactor.core.publisher.Mono;

public interface AiWebClientPort {
    Mono<ScenarioResponse> callScenario(ScenarioDto scenarioDto);
}
