package kt.aivle.shorts.application.port.in;

import kt.aivle.shorts.adapter.in.web.dto.ScenarioResponse;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import reactor.core.publisher.Mono;

public interface ShortsUseCase {
    Mono<ScenarioResponse> createScenario(CreateScenarioCommand command);
}
