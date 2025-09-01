package kt.aivle.analytics.adapter.in.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestMessage {
    private String action;
    private Long postId;
    private Long accountId;
    private Long storeId;
}
