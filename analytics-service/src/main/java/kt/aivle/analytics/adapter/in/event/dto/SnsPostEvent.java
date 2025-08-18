package kt.aivle.analytics.adapter.in.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SnsPostEvent {
    private Long postId;
    private Long accountId;
    private String snsPostId;
}
