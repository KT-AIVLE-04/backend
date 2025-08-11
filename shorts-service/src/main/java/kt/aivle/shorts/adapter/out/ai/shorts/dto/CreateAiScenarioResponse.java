package kt.aivle.shorts.adapter.out.ai.shorts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateAiScenarioResponse(
        @JsonProperty("session_id") String sessionId,
        List<ScenarioItem> scenarios
) {
    public record ScenarioItem(String title, String content) {
    }
}
