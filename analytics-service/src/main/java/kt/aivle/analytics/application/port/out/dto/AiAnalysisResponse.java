package kt.aivle.analytics.application.port.out.dto;

import java.util.List;

import kt.aivle.analytics.domain.model.SentimentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 외부 AI 분석 서버로부터 받는 응답 DTO
 * 감정분석 결과와 키워드를 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AiAnalysisResponse {
    private List<IndividualResult> individual_results;  // AI 서버 응답 필드명에 맞춤
    private Keywords keywords;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualResult {
        private Long id;                // 댓글 ID (DB ID) - AI 서버에서 숫자로 보내고 있음
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
