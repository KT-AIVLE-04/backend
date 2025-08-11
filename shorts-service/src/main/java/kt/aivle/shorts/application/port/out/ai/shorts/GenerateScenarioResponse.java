package kt.aivle.shorts.application.port.out.ai.shorts;

import java.util.List;

public record GenerateScenarioResponse(String sessionId, List<ScenarioItem> scenarios) {
    public record ScenarioItem(String title, String content) {
    }
}