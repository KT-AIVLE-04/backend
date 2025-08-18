package kt.aivle.analytics.adapter.in.web.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCommentsQueryResponse {
    private String commentId;      // YouTube에서 제공하는 실제 comment ID
    private Long authorId;         // 댓글 작성자 ID
    private String text;           // 댓글 내용
    private Long likeCount;        // 댓글 좋아요 수
    private LocalDateTime publishedAt; // 댓글 작성 시간
    private LocalDateTime crawledAt;   // 수집 시간
}
