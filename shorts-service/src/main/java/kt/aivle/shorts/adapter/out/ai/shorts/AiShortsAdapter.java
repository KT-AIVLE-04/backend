package kt.aivle.shorts.adapter.out.ai.shorts;

import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiScenarioRequest;
import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiScenarioResponse;
import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiSceneRequest;
import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiSceneResponse;
import kt.aivle.shorts.adapter.out.ai.shorts.mapper.AiShortsMapper;
import kt.aivle.shorts.application.port.out.ai.shorts.AiShortsPort;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioResponse;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateSceneRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateSceneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AiShortsAdapter implements AiShortsPort {

    private final WebClient aiWebClient;
    private final AiShortsMapper mapper;

    @Override
    public Mono<GenerateScenarioResponse> generateScenario(GenerateScenarioRequest request) {
        CreateAiScenarioRequest createAiScenarioRequest = mapper.toAiCreateScenarioRequest(request);
        return aiWebClient.post()
                .uri("/api/shorts/agent/scenarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createAiScenarioRequest)
                .retrieve()
                .bodyToMono(CreateAiScenarioResponse.class)
                .map(mapper::toGenerateScenarioResponse);
    }

    @Override
    public Mono<GenerateSceneResponse> generateScene(GenerateSceneRequest request) {
        CreateAiSceneRequest createSceneRequest = mapper.toAiCreateSceneRequest(request);
        return aiWebClient.post()
                .uri("/api/shorts/agent/action-scenes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createSceneRequest)
                .retrieve()
                .bodyToMono(CreateAiSceneResponse.class)
                .map(mapper::toGenerateSceneResponse);
    }
}
