package kt.aivle.analytics.application.port.out.dto;

import java.util.List;

import kt.aivle.analytics.domain.model.SentimentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 외부 AI 분석 서버로부터 받는 응답 DTO
 * 감정분석 결과와 키워드를 포함
 */
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
        private String id;              // 댓글 ID (DB ID)
        private SentimentType result;   // 감정분석 결과
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Keywords {
        private List<String> positive;  // 긍정 키워드 목록
        private List<String> negative;  // 부정 키워드 목록
    }
}
