package kt.aivle.analytics.application.port.in;

import kt.aivle.analytics.adapter.in.event.dto.SocialPostResponseEvent;
import kt.aivle.analytics.adapter.out.event.SnsTokenResponseEvent;

public interface AnalyticsEventUseCase {
    void handleSnsTokenResponse(SnsTokenResponseEvent event);
    void handleSocialPostResponse(SocialPostResponseEvent event);
}
