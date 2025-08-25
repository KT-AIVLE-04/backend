package kt.aivle.analytics.application.port.in.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class AccountMetricsQueryRequest extends BaseQueryRequest {
    
    private Long accountId; // SNS 계정 ID
    
    // 편의 메서드들
    
    public static AccountMetricsQueryRequest forDateAndAccountId(LocalDate date, Long accountId) {
        return new AccountMetricsQueryRequest(date, accountId);
    }
    
    public static AccountMetricsQueryRequest forCurrentDateAndAccountId(Long accountId) {
        return new AccountMetricsQueryRequest(null, accountId);
    }
    
    // 생성자
    public AccountMetricsQueryRequest(LocalDate date, Long accountId) {
        super(date);
        this.accountId = accountId;
    }
}
