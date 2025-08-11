package kt.aivle.shorts.application.port.out.ai.shorts.dto;

import java.util.List;

public record GenerateScenarioResponse(String sessionId, List<ScenarioItem> scenarios) {
    public record ScenarioItem(String title, String content) {
    }
}