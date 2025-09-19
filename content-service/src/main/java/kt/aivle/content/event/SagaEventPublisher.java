package kt.aivle.content.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventPublisher {

    private final KafkaTemplate<String, Object> eventKafkaTemplate;

    public void publishContentSaved(String sagaId, String requestId, Long userId, Long storeId, String key) {
        SagaEvent event = SagaEvent.success(sagaId, requestId, SagaEventType.CONTENT_SAVED,
                                          userId, storeId, key);

        eventKafkaTemplate.send("saga.content.saved", sagaId, event);
        log.info("Published CONTENT_SAVED event for saga: {}", sagaId);
    }

    public void publishContentSaveFailed(String sagaId, String requestId, Long userId, Long storeId,
                                       String key, String errorMessage) {
        SagaEvent event = SagaEvent.failure(sagaId, requestId, SagaEventType.CONTENT_SAVE_FAILED,
                                          userId, storeId, key, errorMessage);

        eventKafkaTemplate.send("saga.content.save.failed", sagaId, event);
        log.error("Published CONTENT_SAVE_FAILED event for saga: {}, error: {}", sagaId, errorMessage);
    }

    public void publishContentRollbackCompleted(String sagaId, String requestId, Long userId, Long storeId, String key) {
        SagaEvent event = SagaEvent.success(sagaId, requestId, SagaEventType.CONTENT_ROLLBACK_COMPLETED,
                                          userId, storeId, key);

        eventKafkaTemplate.send("saga.content.rollback.completed", sagaId, event);
        log.info("Published CONTENT_ROLLBACK_COMPLETED event for saga: {}", sagaId);
    }
}