package kt.aivle.analytics.adapter.in.web.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentsQueryResponse {
    private String snsCommentId;  // YouTube에서 제공하는 실제 comment ID
    private String snsPostId;     // YouTube video ID
    private String content;       // 댓글 내용
    private LocalDateTime crawledAt;  // 수집 시간
}
