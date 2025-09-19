package kt.aivle.content.event;

import java.time.LocalDateTime;

public record SagaEvent(
        String sagaId,
        String requestId,
        SagaEventType eventType,
        Long userId,
        Long storeId,
        String key,
        String errorMessage,
        LocalDateTime timestamp
) {
    public static SagaEvent success(String sagaId, String requestId, SagaEventType eventType,
                                   Long userId, Long storeId, String key) {
        return new SagaEvent(sagaId, requestId, eventType, userId, storeId, key, null, LocalDateTime.now());
    }

    public static SagaEvent failure(String sagaId, String requestId, SagaEventType eventType,
                                   Long userId, Long storeId, String key, String errorMessage) {
        return new SagaEvent(sagaId, requestId, eventType, userId, storeId, key, errorMessage, LocalDateTime.now());
    }
}