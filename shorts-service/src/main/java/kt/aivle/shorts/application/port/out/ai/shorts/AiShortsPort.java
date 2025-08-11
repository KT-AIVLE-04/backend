package kt.aivle.shorts.application.port.out.ai.shorts;

import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioResponse;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateSceneRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateSceneResponse;
import reactor.core.publisher.Mono;

public interface AiShortsPort {
    Mono<GenerateScenarioResponse> generateScenario(GenerateScenarioRequest request);

    Mono<GenerateSceneResponse> generateScene(GenerateSceneRequest request);
}
