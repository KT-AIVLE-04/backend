package kt.aivle.shorts.adapter.out.event.saga;

public enum SagaState {
    STARTED,
    SHORTS_CREATED,
    CONTENT_SAVED,
    ANALYTICS_UPDATED,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}