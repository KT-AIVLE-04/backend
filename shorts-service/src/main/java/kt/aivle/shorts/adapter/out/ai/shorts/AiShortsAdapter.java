package kt.aivle.shorts.adapter.out.ai.shorts;

import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiScenarioRequest;
import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiScenarioResponse;
import kt.aivle.shorts.adapter.out.ai.shorts.mapper.AiShortsMapper;
import kt.aivle.shorts.application.port.out.ai.shorts.AiShortsPort;
import kt.aivle.shorts.application.port.out.ai.shorts.GenerateScenarioRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.GenerateScenarioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiShortsAdapter implements AiShortsPort {

    private final WebClient aiWebClient;
    private final AiShortsMapper mapper;

    @Override
    public Mono<GenerateScenarioResponse> generateScenario(GenerateScenarioRequest request) {
        CreateAiScenarioRequest createAiScenarioRequest = mapper.toCreateScenarioRequest(request);
        return aiWebClient.post()
                .uri("/api/shorts/agent/scenarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createAiScenarioRequest)
                .retrieve()
                .bodyToMono(CreateAiScenarioResponse.class)
                .map(mapper::toAiScenarioResponse);
    }

//    @Override
//    public Mono<SceneResponse> callScene(CreateSceneRequestDto request) {
//        return aiWebClient.post()
//                .uri("/api/shorts/agent/scenarios")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(Scenario.class);
//    }
}
