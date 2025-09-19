package kt.aivle.analytics.adapter.in.event.consumer;

import kt.aivle.analytics.adapter.in.event.SagaEventPublisher;
import kt.aivle.analytics.adapter.in.event.dto.SagaEvent;
import kt.aivle.analytics.application.service.AnalyticsEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventConsumer {

    private final AnalyticsEventService analyticsEventService;
    private final SagaEventPublisher sagaEventPublisher;

    @KafkaListener(topics = "saga.analytics.update", groupId = "analytics-saga-group")
    @Transactional
    public void handleAnalyticsUpdate(SagaEvent event) {
        String sagaId = event.sagaId();
        String requestId = event.requestId();

        log.info("Received AnalyticsUpdate event: sagaId={}, requestId={}", sagaId, requestId);

        try {
            // 분석 데이터 업데이트 로직
            updateAnalyticsData(event);

            log.info("Successfully updated analytics for saga: {}", sagaId);
            sagaEventPublisher.publishAnalyticsUpdated(sagaId, requestId,
                    event.userId(), event.storeId(), event.key());

        } catch (Exception e) {
            log.error("Failed to update analytics for saga: {}, error: {}", sagaId, e.getMessage(), e);
            sagaEventPublisher.publishAnalyticsUpdateFailed(sagaId, requestId,
                    event.userId(), event.storeId(), event.key(), e.getMessage());
        }
    }

    @KafkaListener(topics = "saga.analytics.rollback", groupId = "analytics-saga-group")
    @Transactional
    public void handleAnalyticsRollback(SagaEvent event) {
        String sagaId = event.sagaId();
        log.info("Received AnalyticsRollback event: sagaId={}", sagaId);

        try {
            // 분석 데이터 롤백 로직
            rollbackAnalyticsData(event);

            log.info("Successfully completed analytics rollback for saga: {}", sagaId);
            sagaEventPublisher.publishAnalyticsRollbackCompleted(sagaId, event.requestId(),
                    event.userId(), event.storeId(), event.key());

        } catch (Exception e) {
            log.error("Failed to rollback analytics for saga: {}, error: {}", sagaId, e.getMessage(), e);
        }
    }

    private void updateAnalyticsData(SagaEvent event) {
        // 실제 분석 데이터 업데이트 로직
        // 예: 콘텐츠 생성 횟수 증가, 사용자별 통계 업데이트 등
        log.info("Updating analytics data for userId: {}, storeId: {}", event.userId(), event.storeId());

        // 예시: 콘텐츠 생성 이벤트 처리
        // analyticsEventService.processContentCreationEvent(event.userId(), event.storeId(), event.key());
    }

    private void rollbackAnalyticsData(SagaEvent event) {
        // 분석 데이터 롤백 로직
        log.info("Rolling back analytics data for userId: {}, storeId: {}", event.userId(), event.storeId());

        // 예시: 콘텐츠 생성 통계 롤백
        // analyticsEventService.rollbackContentCreationEvent(event.userId(), event.storeId(), event.key());
    }
}