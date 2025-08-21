package kt.aivle.analytics.application.port.out.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 외부 AI 분석 서버로 보내는 요청 DTO
 * 댓글 데이터와 기존 키워드를 포함하여 감정분석을 요청
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisRequest {
    private List<CommentData> data;
    private Keywords keyword;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentData {
        private String id;        // 댓글 ID
        private String result;    // 댓글 내용
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
