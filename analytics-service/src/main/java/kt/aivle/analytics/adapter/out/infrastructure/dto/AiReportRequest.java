package kt.aivle.analytics.adapter.out.infrastructure.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReportRequest {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        @JsonProperty("post_id")
        private Long postId;
        
        @JsonProperty("view_count")
        private Long viewCount;
        
        @JsonProperty("like_count")
        private Long likeCount;
        
        @JsonProperty("comment_count")
        private Long commentCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionData {
        @JsonProperty("positive_count")
        private Long positiveCount;
        
        @JsonProperty("negative_count")
        private Long negativeCount;
        
        @JsonProperty("neutral_count")
        private Long neutralCount;
        
        @JsonProperty("positive_keywords")
        private List<String> positiveKeywords;
        
        @JsonProperty("negative_keywords")
        private List<String> negativeKeywords;
        
        @JsonProperty("neutral_keywords")
        private List<String> neutralKeywords;
    }
    
    private Metrics metrics;
    
    @JsonProperty("emotion_data")
    private EmotionData emotionData;
    
    private String title;
    private String description;
    private String url;
    private List<String> tags;
    
    @JsonProperty("publish_at")
    private String publishAt;
}
