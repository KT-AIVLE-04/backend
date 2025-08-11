package kt.aivle.shorts.application.port.in;

import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.command.CreateSceneCommand;
import kt.aivle.shorts.application.port.in.dto.ScenarioDTO;
import kt.aivle.shorts.application.port.in.dto.SceneDTO;
import reactor.core.publisher.Mono;

public interface ShortsUseCase {
    Mono<ScenarioDTO> createScenario(CreateScenarioCommand command);

    Mono<SceneDTO> createScene(CreateSceneCommand command);
}
