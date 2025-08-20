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
public class AiAnalysisResponse {
    private EmotionAnalysis emotionAnalysis;
    private Keywords keywords;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionAnalysis {
        private List<IndividualResult> individualResults;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualResult {
        private String id;
        private String result;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Keywords {
        private List<String> positive;
        private List<String> negative;
    }
}
