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
    private List<CommentData> comments;
    private Keywords keywords;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentData {
        private Long id;                    // 댓글 ID (DB ID)
        private String created_at;          // 생성 시간
        private String author_id;           // 작성자 ID
        private String content;             // 댓글 내용
        private Integer like_count;         // 좋아요 수
        private Long post_id;               // 게시물 ID
        private String published_at;        // 발행 시간
        private String sns_comment_id;      // SNS 댓글 ID
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
