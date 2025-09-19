package kt.aivle.analytics.adapter.in.event;

import kt.aivle.analytics.adapter.in.event.dto.SagaEvent;
import kt.aivle.analytics.adapter.in.event.dto.SagaEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventPublisher {

    private final KafkaTemplate<String, Object> sagaKafkaTemplate;

    public void publishAnalyticsUpdated(String sagaId, String requestId, Long userId, Long storeId, String key) {
        SagaEvent event = SagaEvent.success(sagaId, requestId, SagaEventType.ANALYTICS_UPDATED,
                                          userId, storeId, key);

        sagaKafkaTemplate.send("saga.analytics.updated", sagaId, event);
        log.info("Published ANALYTICS_UPDATED event for saga: {}", sagaId);
    }

    public void publishAnalyticsUpdateFailed(String sagaId, String requestId, Long userId, Long storeId,
                                           String key, String errorMessage) {
        SagaEvent event = SagaEvent.failure(sagaId, requestId, SagaEventType.ANALYTICS_UPDATE_FAILED,
                                          userId, storeId, key, errorMessage);

        sagaKafkaTemplate.send("saga.analytics.update.failed", sagaId, event);
        log.error("Published ANALYTICS_UPDATE_FAILED event for saga: {}, error: {}", sagaId, errorMessage);
    }

    public void publishAnalyticsRollbackCompleted(String sagaId, String requestId, Long userId, Long storeId, String key) {
        SagaEvent event = SagaEvent.success(sagaId, requestId, SagaEventType.ANALYTICS_ROLLBACK_COMPLETED,
                                          userId, storeId, key);

        sagaKafkaTemplate.send("saga.analytics.rollback.completed", sagaId, event);
        log.info("Published ANALYTICS_ROLLBACK_COMPLETED event for saga: {}", sagaId);
    }
}