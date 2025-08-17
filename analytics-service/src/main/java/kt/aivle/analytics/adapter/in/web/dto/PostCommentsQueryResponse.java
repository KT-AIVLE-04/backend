package kt.aivle.analytics.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentsQueryResponse {
    private String snsCommentId;
    private String snsPostId;
    private Long accountId;
    private String content;
    private LocalDateTime crawledAt;
}
