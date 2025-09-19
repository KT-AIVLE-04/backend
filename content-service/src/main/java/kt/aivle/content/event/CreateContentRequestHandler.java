package kt.aivle.content.event;

import kt.aivle.content.service.ContentService;
import kt.aivle.content.service.MessageIdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateContentRequestHandler {

    private final ContentService contentService;
    private final MessageIdempotencyService messageIdempotencyService;

    @KafkaListener(
            topics = "${topic.content.request}",
            groupId = "content-group",
            containerFactory = "requestListenerFactory"
    )
    @SendTo
    @Transactional
    public Ack handle(CreateContentRequestMessage req) {
        String requestId = req.requestId();

        if (requestId == null || requestId.trim().isEmpty()) {
            log.warn("Missing requestId in message: {}", req);
            return new Ack(false, "requestId is required for idempotency");
        }

        if (messageIdempotencyService.isAlreadyProcessed(requestId)) {
            log.info("Message already processed: {}", requestId);
            return new Ack(true, "Already processed");
        }

        try {
            contentService.uploadContent(req);
            messageIdempotencyService.markAsProcessed(requestId);
            log.info("Successfully processed message: {}", requestId);
            return new Ack(true, null);
        } catch (Exception e) {
            log.error("Failed to process message: {}, error: {}", requestId, e.getMessage(), e);
            return new Ack(false, e.getMessage());
        }
    }
}