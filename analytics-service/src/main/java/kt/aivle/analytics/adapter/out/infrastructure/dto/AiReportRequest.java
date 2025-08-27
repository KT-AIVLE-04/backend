package kt.aivle.analytics.adapter.out.infrastructure.dto;

import java.util.List;

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
        private Long post_id;
        private Long view_count;
        private Long like_count;
        private Long comment_count;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionData {
        private Long positive_count;
        private Long negative_count;
        private Long neutral_count;
        private List<String> positive_keywords;
        private List<String> negative_keywords;
        private List<String> neutral_keywords;
    }
    
    private Metrics metrics;
    private EmotionData emotion_data;
}
