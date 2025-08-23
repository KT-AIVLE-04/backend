package kt.aivle.analytics.adapter.in.web.dto.response;

import java.time.LocalDateTime;

import kt.aivle.analytics.domain.model.SnsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMetricsResponse {
    private Long accountId;        // Local DB ID
    private Long followers;        // 실시간 구독자 수
    private Long views;            // 실시간 총 조회 수
    private LocalDateTime fetchedAt; // API 호출 시간
    private SnsType snsType;       // SNS 플랫폼 타입
    private Boolean isCached;      // 캐시된 데이터인지 여부
}
