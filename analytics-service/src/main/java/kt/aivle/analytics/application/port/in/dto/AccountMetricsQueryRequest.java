package kt.aivle.analytics.application.port.in.dto;

import java.util.Date;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public class AccountMetricsQueryRequest extends BaseQueryRequest {
    
    @Pattern(regexp = "^\\d+$", message = "AccountId must be a valid number")
    private String accountId; // null이면 모든 계정
    
    // 편의 메서드들
    public static AccountMetricsQueryRequest forCurrentDate(String accountId) {
        return new AccountMetricsQueryRequest(null, accountId);
    }
    
    public static AccountMetricsQueryRequest forDate(Date date, String accountId) {
        return new AccountMetricsQueryRequest(date, accountId);
    }
    
    public static AccountMetricsQueryRequest forAllAccounts(Date date) {
        return new AccountMetricsQueryRequest(date, null);
    }
    
    // 생성자
    public AccountMetricsQueryRequest(Date date, String accountId) {
        super(date);
        this.accountId = accountId;
    }
    
    // 검증 메서드
    public boolean isValidAccountId() {
        return accountId == null || accountId.matches("\\d+");
    }
}
