package kt.aivle.analytics.adapter.in.web.dto.response;

import java.util.HashMap;
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
    
    @Builder.Default
    private Map<String, List<String>> keywords = new HashMap<>();  // 기본값: 빈 맵
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionSummary {
        @Builder.Default
        private Long positiveCount = 0L;  // 기본값: 0
        
        @Builder.Default
        private Long neutralCount = 0L;   // 기본값: 0
        
        @Builder.Default
        private Long negativeCount = 0L;  // 기본값: 0
        
        @Builder.Default
        private Long totalCount = 0L;     // 기본값: 0
    }
}
