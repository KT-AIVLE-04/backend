package kt.aivle.analytics.application.port.in;

import kt.aivle.analytics.adapter.in.event.dto.SnsPostEvent;
import kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent;

public interface AnalyticsEventUseCase {
    void handlePostCreated(SnsPostEvent event);
    void handlePostDeleted(SnsPostEvent event);
    void handleSnsAccountConnected(SnsAccountEvent event);
    void handleSnsAccountDisconnected(SnsAccountEvent event);
}
