package kt.aivle.analytics.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentsQueryRequest {
    private String dateRange; // "today", "3days", "1week", "1month", "6months", "1year"
    private String postId; // null이면 모든 게시물
}
