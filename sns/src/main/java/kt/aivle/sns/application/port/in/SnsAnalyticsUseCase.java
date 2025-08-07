package kt.aivle.sns.application.port.in;

import kt.aivle.sns.domain.model.SnsAnalyticsRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.domain.model.YoutubeAnalyticsResponse;

public interface SnsAnalyticsUseCase {
    SnsType supportSnsType();

    YoutubeAnalyticsResponse getAnalytics(String userId, SnsAnalyticsRequest request);
}
