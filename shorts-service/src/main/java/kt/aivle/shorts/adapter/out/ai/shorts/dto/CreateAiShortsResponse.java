package kt.aivle.shorts.adapter.out.ai.shorts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateAiShortsResponse(@JsonProperty("video_url") String videoUrl) {
}