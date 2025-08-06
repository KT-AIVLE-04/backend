package kt.aivle.shorts.application.port.in;

import kt.aivle.shorts.adapter.in.web.dto.ScenarioResponse;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ShortsUseCase {
    Mono<List<ScenarioResponse>> createScenario(CreateScenarioCommand command);
}
