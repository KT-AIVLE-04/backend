package kt.aivle.shorts.adapter.out.event.saga;

import java.time.LocalDateTime;

public class SagaInstance {
    private String sagaId;
    private String requestId;
    private SagaState state;
    private Long userId;
    private Long storeId;
    private String key;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SagaInstance(String sagaId, String requestId, Long userId, Long storeId, String key) {
        this.sagaId = sagaId;
        this.requestId = requestId;
        this.userId = userId;
        this.storeId = storeId;
        this.key = key;
        this.state = SagaState.STARTED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateState(SagaState newState) {
        this.state = newState;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateState(SagaState newState, String errorMessage) {
        this.state = newState;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public String getSagaId() { return sagaId; }
    public String getRequestId() { return requestId; }
    public SagaState getState() { return state; }
    public Long getUserId() { return userId; }
    public Long getStoreId() { return storeId; }
    public String getKey() { return key; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public boolean isCompleted() {
        return state == SagaState.COMPLETED;
    }

    public boolean isFailed() {
        return state == SagaState.FAILED;
    }

    public boolean needsCompensation() {
        return state == SagaState.FAILED && !isInCompensation();
    }

    public boolean isInCompensation() {
        return state == SagaState.COMPENSATING || state == SagaState.COMPENSATED;
    }
}