package kt.aivle.analytics.adapter.in.web.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import kt.aivle.analytics.domain.model.SentimentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResponse {
    @JsonProperty("emotion_analysis")
    private EmotionAnalysis emotionAnalysis;
    private Keywords keywords;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionAnalysis {
        @JsonProperty("individual_results")
        private List<IndividualResult> individualResults;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualResult {
        private String id;
        private SentimentType result;  // POSITIVE, NEGATIVE, NEUTRAL enum으로 받음
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
