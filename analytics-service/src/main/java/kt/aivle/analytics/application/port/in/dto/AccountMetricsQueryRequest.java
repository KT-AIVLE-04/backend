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
    
    // 검증 메서드
    public boolean isValidRequest() {
        if (snsType != null && userId != null) {
            return userId.matches("\\d+");
        }
        return true; // 히스토리 API에서는 snsType이 선택사항이므로
    }
    
    // 실시간 API용 검증 (snsType과 userId가 필수)
    public boolean isValidRealtimeRequest() {
        return snsType != null && userId != null && userId.matches("\\d+");
    }
}
