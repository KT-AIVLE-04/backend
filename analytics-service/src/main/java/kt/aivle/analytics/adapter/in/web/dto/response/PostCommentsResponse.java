package kt.aivle.analytics.adapter.in.web.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCommentsResponse {
    @Builder.Default
    private String commentId = "";      // YouTube에서 제공하는 실제 comment ID (기본값: 빈 문자열)
    
    @Builder.Default
    private String authorId = "";       // 댓글 작성자 ID (기본값: 빈 문자열)
    
    @Builder.Default
    private String text = "";           // 댓글 내용 (기본값: 빈 문자열)
    
    @Builder.Default
    private Long likeCount = 0L;        // 댓글 좋아요 수 (기본값: 0)
    
    private LocalDateTime publishedAt; // 댓글 작성 시간
}
