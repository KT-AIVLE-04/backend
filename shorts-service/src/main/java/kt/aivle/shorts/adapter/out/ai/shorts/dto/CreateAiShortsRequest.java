package kt.aivle.shorts.adapter.out.ai.shorts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateAiShortsRequest(
        @JsonProperty("session_id") String sessionId,
        String title,
        String content,
        @JsonProperty("ad_duration") Integer adDuration,
        @JsonProperty("image_list") List<String> imageUrls
) {
}