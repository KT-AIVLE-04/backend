package kt.aivle.analytics.application.port.in.dto;

import java.time.LocalDate;

import kt.aivle.analytics.domain.model.SnsType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class AccountMetricsQueryRequest extends BaseQueryRequest {
    
    private SnsType snsType; // SNS 타입 (실시간 API에서는 필수)
    private String userId;   // 사용자 ID (실시간 API에서는 필수)
    
    // 편의 메서드들
    
    public static AccountMetricsQueryRequest forDateAndSnsType(LocalDate date, String userId, SnsType snsType) {
        return new AccountMetricsQueryRequest(date, snsType, userId);
    }
    
    public static AccountMetricsQueryRequest forCurrentDateAndSnsType(String userId, SnsType snsType) {
        return new AccountMetricsQueryRequest(null, snsType, userId);
    }
    
    // 생성자
    public AccountMetricsQueryRequest(LocalDate date, SnsType snsType, String userId) {
        super(date);
        this.snsType = snsType;
        this.userId = userId;
    }
}
