package kt.aivle.analytics.adapter.in.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SnsAccountEvent {
    private Long accountId;
    private Long userId;
    private String snsAccountId;
    private String type;
}
