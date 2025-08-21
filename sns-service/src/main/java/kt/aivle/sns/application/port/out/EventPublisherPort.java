package kt.aivle.sns.application.port.out;

import kt.aivle.sns.application.event.PostEvent;
import kt.aivle.sns.application.event.SnsAccountEvent;

public interface EventPublisherPort {
    void publishPostCreated(PostEvent e);
    void publishPostDeleted(PostEvent e);
    void publishSnsAccountConnected(SnsAccountEvent e);
    void publishSnsAccountDisconnected(SnsAccountEvent e);
}
