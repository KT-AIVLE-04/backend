package kt.aivle.sns.application.event;

import kt.aivle.sns.domain.model.SnsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnsAccountEvent {
    private Long id;          // 내 DB의 sns_account PK
    private Long userId;
    private String snsAccountId; // 예: 유튜브 채널ID
    private SnsType type;
}
