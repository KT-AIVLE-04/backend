package kt.aivle.shorts.adapter.out.event.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaOrchestrator {

    private final KafkaTemplate<String, Object> eventKafkaTemplate;
    private final ShortsCompensationService shortsCompensationService;
    private final ConcurrentMap<String, SagaInstance> sagaInstances = new ConcurrentHashMap<>();

    public void startSaga(String sagaId, String requestId, Long userId, Long storeId, String key) {
        SagaInstance saga = new SagaInstance(sagaId, requestId, userId, storeId, key);
        sagaInstances.put(sagaId, saga);

        // 1단계: ShortsCreated 이벤트 발행
        SagaEvent event = SagaEvent.success(sagaId, requestId, SagaEventType.SHORTS_CREATED,
                                          userId, storeId, key);

        saga.updateState(SagaState.SHORTS_CREATED);
        publishEvent("saga.shorts.created", event);
        log.info("Saga started: {}", sagaId);
    }

    @KafkaListener(topics = "saga.content.saved", groupId = "shorts-saga-group")
    public void handleContentSaved(SagaEvent event) {
        SagaInstance saga = sagaInstances.get(event.sagaId());
        if (saga == null) {
            log.warn("Unknown saga: {}", event.sagaId());
            return;
        }

        saga.updateState(SagaState.CONTENT_SAVED);
        log.info("Content saved for saga: {}", event.sagaId());

        // 3단계: Analytics 업데이트 요청
        SagaEvent analyticsEvent = SagaEvent.success(event.sagaId(), event.requestId(),
                                                   SagaEventType.ANALYTICS_UPDATED,
                                                   event.userId(), event.storeId(), event.key());
        publishEvent("saga.analytics.update", analyticsEvent);
    }

    @KafkaListener(topics = "saga.content.save.failed", groupId = "shorts-saga-group")
    public void handleContentSaveFailed(SagaEvent event) {
        SagaInstance saga = sagaInstances.get(event.sagaId());
        if (saga == null) {
            log.warn("Unknown saga: {}", event.sagaId());
            return;
        }

        saga.updateState(SagaState.FAILED, event.errorMessage());
        log.error("Content save failed for saga: {}, error: {}", event.sagaId(), event.errorMessage());

        // 보상 트랜잭션 시작
        startCompensation(saga);
    }

    @KafkaListener(topics = "saga.analytics.updated", groupId = "shorts-saga-group")
    public void handleAnalyticsUpdated(SagaEvent event) {
        SagaInstance saga = sagaInstances.get(event.sagaId());
        if (saga == null) {
            log.warn("Unknown saga: {}", event.sagaId());
            return;
        }

        saga.updateState(SagaState.COMPLETED);
        log.info("Saga completed successfully: {}", event.sagaId());

        // Saga 인스턴스 정리
        sagaInstances.remove(event.sagaId());
    }

    @KafkaListener(topics = "saga.analytics.update.failed", groupId = "shorts-saga-group")
    public void handleAnalyticsUpdateFailed(SagaEvent event) {
        SagaInstance saga = sagaInstances.get(event.sagaId());
        if (saga == null) {
            log.warn("Unknown saga: {}", event.sagaId());
            return;
        }

        saga.updateState(SagaState.FAILED, event.errorMessage());
        log.error("Analytics update failed for saga: {}, error: {}", event.sagaId(), event.errorMessage());

        // 보상 트랜잭션 시작
        startCompensation(saga);
    }

    private void startCompensation(SagaInstance saga) {
        saga.updateState(SagaState.COMPENSATING);
        log.info("Starting compensation for saga: {}", saga.getSagaId());

        // Content 삭제 요청 (Content가 저장된 경우에만)
        if (saga.getState() == SagaState.CONTENT_SAVED ||
            saga.getState() == SagaState.ANALYTICS_UPDATED) {

            SagaEvent compensateEvent = SagaEvent.success(saga.getSagaId(), saga.getRequestId(),
                                                        SagaEventType.CONTENT_ROLLBACK_COMPLETED,
                                                        saga.getUserId(), saga.getStoreId(), saga.getKey());
            publishEvent("saga.content.rollback", compensateEvent);
        }

        // Shorts 자체 보상 트랜잭션 실행
        try {
            shortsCompensationService.compensateShortsCreation(
                    saga.getSagaId(), saga.getUserId(), saga.getStoreId(), saga.getKey());
        } catch (Exception e) {
            log.error("Failed to compensate shorts creation for saga: {}, error: {}",
                    saga.getSagaId(), e.getMessage(), e);
        }

        saga.updateState(SagaState.COMPENSATED);
        sagaInstances.remove(saga.getSagaId());
    }

    private void publishEvent(String topic, SagaEvent event) {
        eventKafkaTemplate.send(topic, event.sagaId(), event);
        log.debug("Published event: {} to topic: {}", event.eventType(), topic);
    }

    public SagaInstance getSagaInstance(String sagaId) {
        return sagaInstances.get(sagaId);
    }
}