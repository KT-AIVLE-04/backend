package kt.aivle.shorts.adapter.out.ai;

import kt.aivle.shorts.adapter.in.web.dto.ScenarioResponse;
import kt.aivle.shorts.application.port.out.AiWebClientPort;
import kt.aivle.shorts.application.service.dto.ScenarioDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AiWebClientAdapter implements AiWebClientPort {

    private final WebClient aiWebClient;

    @Override
    public Mono<ScenarioResponse> callScenario(ScenarioDto scenarioDto) {
        return aiWebClient.post()
                .uri("/api/shorts/agent/scenarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(scenarioDto)
                .retrieve()
                .bodyToMono(ScenarioResponse.class);
    }
}
