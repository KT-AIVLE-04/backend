package kt.aivle.shorts.application.port.in;

import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.dto.ScenarioDTO;
import reactor.core.publisher.Mono;

public interface ShortsUseCase {
    Mono<ScenarioDTO> createScenario(CreateScenarioCommand command);

//    Mono<SceneResEponse> createScene(CreateSceneCommand command);
}
