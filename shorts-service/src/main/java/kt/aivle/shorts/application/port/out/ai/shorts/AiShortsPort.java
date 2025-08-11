package kt.aivle.shorts.application.port.out.ai.shorts;

import reactor.core.publisher.Mono;

public interface AiShortsPort {
    Mono<GenerateScenarioResponse> generateScenario(GenerateScenarioRequest request);

//    Mono<SceneResponse> callScene(CreateSceneCommand command);
}
