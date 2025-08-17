package kt.aivle.analytics.adapter.in.web.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostMetricsQueryResponse {
    private Long postId;
    private String snsPostId;
    private Long accountId;
    private String likes;
    private Long dislikes;
    private Long comments;
    private Long shares;
    private Long views;
    private LocalDateTime crawledAt;
}
