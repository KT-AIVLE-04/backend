package kt.aivle.shorts.adapter.out.ai.shorts;

import kt.aivle.shorts.application.port.out.ai.shorts.AiShortsPort;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioResponse;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioResponse.ScenarioItem;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateShortsRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateShortsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@Profile("mock")
public class MockAiShortsAdapter implements AiShortsPort {

    @Override
    public Mono<GenerateScenarioResponse> generateScenario(GenerateScenarioRequest request) {
        log.info("Mock AI Scenario generation for store: {}", request.storeName());
        
        return Mono.delay(Duration.ofMillis(1500)) // 1.5초 AI 처리 시뮬레이션
                .map(tick -> new GenerateScenarioResponse(
                        "mock-session-" + System.currentTimeMillis(),
                        List.of(
                                new ScenarioItem("Mock Scenario 1", "Mock script content for " + request.storeName()),
                                new ScenarioItem("Mock Scenario 2", "Alternative mock script with product benefits")
                        )
                ));
    }

    @Override
    public Mono<GenerateShortsResponse> generateShorts(GenerateShortsRequest request) {
        log.info("Mock AI Shorts generation for session: {}", request.sessionId());
        
        return Mono.delay(Duration.ofMillis(2000)) // 2초 AI 처리 시뮬레이션
                .map(tick -> new GenerateShortsResponse(
                        "shorts/result/" + request.sessionId() + ".mp4"
                ));
    }
}