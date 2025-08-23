package kt.aivle.analytics.adapter.in.event.dto;

import kt.aivle.analytics.domain.model.SnsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnsAccountEvent {
    private Long accountId;          // 내 DB의 sns_account PK
    private Long userId;
    private String snsAccountId;     // 예: 유튜브 채널ID
    private SnsType type;            // SNS 타입 (enum)
}
