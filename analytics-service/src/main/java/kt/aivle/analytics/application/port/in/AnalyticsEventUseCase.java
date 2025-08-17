package kt.aivle.analytics.application.port.in;

import kt.aivle.analytics.adapter.in.event.dto.PostEvent;
import kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent;

public interface AnalyticsEventUseCase {
    void handlePostCreated(PostEvent event);
    void handlePostDeleted(PostEvent event);
    void handleSnsAccountConnected(SnsAccountEvent event);
    void handleSnsAccountDisconnected(SnsAccountEvent event);
}
