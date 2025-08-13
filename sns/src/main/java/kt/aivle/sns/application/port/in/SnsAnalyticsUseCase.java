package kt.aivle.sns.application.port.in;

import kt.aivle.sns.adapter.in.web.dto.SnsAnalyticsRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.adapter.in.web.dto.YoutubeAnalyticsResponse;

public interface SnsAnalyticsUseCase {
    SnsType supportSnsType();

    YoutubeAnalyticsResponse getAnalytics(Long userId, SnsAnalyticsRequest request);
}
