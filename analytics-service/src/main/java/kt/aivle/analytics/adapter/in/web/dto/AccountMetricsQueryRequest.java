package kt.aivle.analytics.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountMetricsQueryRequest {
    private String dateRange; // "current", "today", "3days", "1week", "1month", "6months", "1year"
    private String accountId; // null이면 모든 계정
}
