package kt.aivle.analytics.adapter.in.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    
    @JsonProperty("type")
    private String snsTypeString;    // JSON에서 받을 때는 String으로
    
    // 실제 SnsType enum으로 변환하는 getter
    @JsonIgnore
    public SnsType getType() {
        if (snsTypeString == null) return null;
        
        switch (snsTypeString.toLowerCase()) {
            case "youtube":
                return SnsType.YOUTUBE;
            case "instagram":
                return SnsType.INSTAGRAM;
            case "tiktok":
                return SnsType.TIKTOK;
            // 하위 호환성을 위해 twitter도 지원하되 명확하게 문서화
            case "twitter": // deprecated: use "tiktok" instead
                return SnsType.TIKTOK;
            default:
                throw new IllegalArgumentException("Unknown SNS type: " + snsTypeString);
        }
    }
}
