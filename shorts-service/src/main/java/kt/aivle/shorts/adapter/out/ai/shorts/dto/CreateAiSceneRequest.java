package kt.aivle.shorts.adapter.out.ai.shorts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateAiSceneRequest(
        @JsonProperty("session_id") String sessionId,
        String title,
        String content,
        @JsonProperty("ad_duration") Integer adDuration) {
}
