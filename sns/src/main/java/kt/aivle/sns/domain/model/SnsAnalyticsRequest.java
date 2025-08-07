package kt.aivle.sns.domain.model;

import lombok.Getter;

@Getter
public class SnsAnalyticsRequest {
    private String postId;
    private String startDate; // 수집 시작 날짜
    private String endDate; // 수집 끝 날짜
}
