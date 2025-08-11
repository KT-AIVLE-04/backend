package kt.aivle.shorts.application.port.in.dto;

import java.util.List;

public record ScenarioDTO(String sessionId, List<ScenarioItem> scenarios) {
    public record ScenarioItem(String title, String content) {
    }
}
