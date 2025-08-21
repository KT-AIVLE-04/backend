package kt.aivle.shorts.adapter.out.ai.shorts;

import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiScenarioRequest;
import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiScenarioResponse;
import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiShortsRequest;
import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiShortsResponse;
import kt.aivle.shorts.adapter.out.ai.shorts.mapper.AiShortsMapper;
import kt.aivle.shorts.application.port.out.ai.shorts.AiShortsPort;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioResponse;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateShortsRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateShortsResponse;
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
    public Mono<GenerateShortsResponse> generateShorts(GenerateShortsRequest request) {
        CreateAiShortsRequest createAiShortsRequest = mapper.toAiCreateShortsRequest(request);
        return aiWebClient.post()
                .uri("/api/shorts/agent/videos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createAiShortsRequest)
                .retrieve()
                .bodyToMono(CreateAiShortsResponse.class)
                .map(mapper::toGenerateShortsResponse);
    }
}
