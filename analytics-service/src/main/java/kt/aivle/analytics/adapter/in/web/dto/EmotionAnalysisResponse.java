package kt.aivle.analytics.adapter.in.web.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionAnalysisResponse {
    private Long postId;
    private EmotionSummary emotionSummary;
    private Map<String, List<String>> keywords;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionSummary {
        private Long positiveCount;
        private Long neutralCount;
        private Long negativeCount;
        private Long totalCount;
    }
}
