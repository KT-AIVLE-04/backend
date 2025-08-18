package kt.aivle.content.event;

import kt.aivle.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateContentRequestHandler {

    private final ContentService contentService;

    @KafkaListener(
            topics = "${topic.content.request}",
            groupId = "content-group",
            containerFactory = "requestListenerFactory"
    )
    @SendTo
    public Ack handle(CreateContentRequestMessage req) {
        try {
            contentService.uploadContent(req);
            return new Ack(true, null);
        } catch (Exception e) {
            return new Ack(false, e.getMessage());
        }
    }
}