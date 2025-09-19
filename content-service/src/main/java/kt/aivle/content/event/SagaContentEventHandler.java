package kt.aivle.content.event;

import kt.aivle.content.service.ContentService;
import kt.aivle.content.service.MessageIdempotencyService;
import kt.aivle.content.service.ContentCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaContentEventHandler {

    private final ContentService contentService;
    private final MessageIdempotencyService messageIdempotencyService;
    private final ContentCompensationService contentCompensationService;
    private final SagaEventPublisher sagaEventPublisher;

    @KafkaListener(topics = "saga.shorts.created", groupId = "content-saga-group")
    @Transactional
    public void handleShortsCreated(SagaEvent event) {
        String requestId = event.requestId();
        String sagaId = event.sagaId();

        log.info("Received ShortsCreated event: sagaId={}, requestId={}", sagaId, requestId);

        if (requestId == null || requestId.trim().isEmpty()) {
            log.warn("Missing requestId in Saga event: {}", event);
            sagaEventPublisher.publishContentSaveFailed(sagaId, requestId,
                    event.userId(), event.storeId(), event.key(),
                    "requestId is required for idempotency");
            return;
        }

        if (messageIdempotencyService.isAlreadyProcessed(requestId)) {
            log.info("Message already processed for saga: {}, requestId: {}", sagaId, requestId);
            sagaEventPublisher.publishContentSaved(sagaId, requestId,
                    event.userId(), event.storeId(), event.key());
            return;
        }

        try {
            // CreateContentRequestMessage 생성
            CreateContentRequestMessage message = new CreateContentRequestMessage(
                    requestId, event.userId(), event.storeId(), event.key()
            );

            contentService.uploadContent(message);
            messageIdempotencyService.markAsProcessed(requestId);

            log.info("Successfully processed content upload for saga: {}", sagaId);
            sagaEventPublisher.publishContentSaved(sagaId, requestId,
                    event.userId(), event.storeId(), event.key());

        } catch (Exception e) {
            log.error("Failed to process content upload for saga: {}, error: {}", sagaId, e.getMessage(), e);
            sagaEventPublisher.publishContentSaveFailed(sagaId, requestId,
                    event.userId(), event.storeId(), event.key(), e.getMessage());
        }
    }

    @KafkaListener(topics = "saga.content.rollback", groupId = "content-saga-group")
    @Transactional
    public void handleContentRollback(SagaEvent event) {
        String sagaId = event.sagaId();
        log.info("Received ContentRollback event: sagaId={}", sagaId);

        try {
            // 실제 콘텐츠 삭제 로직 실행
            contentCompensationService.compensateContentCreation(
                    event.userId(), event.storeId(), sagaId);

            log.info("Successfully completed content rollback for saga: {}", sagaId);
            sagaEventPublisher.publishContentRollbackCompleted(sagaId, event.requestId(),
                    event.userId(), event.storeId(), event.key());

        } catch (Exception e) {
            log.error("Failed to rollback content for saga: {}, error: {}", sagaId, e.getMessage(), e);
        }
    }
}