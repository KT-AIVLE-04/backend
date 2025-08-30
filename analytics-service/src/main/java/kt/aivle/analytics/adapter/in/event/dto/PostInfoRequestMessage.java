package kt.aivle.analytics.adapter.in.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostInfoRequestMessage {
    private Long postId;
    private Long userId;
    private Long accountId;
    private Long storeId;
}
