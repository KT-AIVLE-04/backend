package kt.aivle.shorts.adapter.in.web.dto.response;

import java.util.List;

public record ScenarioResponse(String sessionId, List<ScenarioItem> scenarios) {
    public record ScenarioItem(String title, String content) {
    }
}
