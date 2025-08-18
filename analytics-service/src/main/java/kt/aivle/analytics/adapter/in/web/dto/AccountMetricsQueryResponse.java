package kt.aivle.analytics.adapter.in.web.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountMetricsQueryResponse {
    private Long accountId;
    private Long followers;
    private Long views;
    private LocalDateTime crawledAt;
}
