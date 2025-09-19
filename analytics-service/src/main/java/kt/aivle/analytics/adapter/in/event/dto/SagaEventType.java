package kt.aivle.analytics.adapter.in.event.dto;

public enum SagaEventType {
    // Shorts 관련 이벤트
    SHORTS_CREATED,
    SHORTS_CREATION_FAILED,
    SHORTS_ROLLBACK_COMPLETED,

    // Content 관련 이벤트
    CONTENT_SAVED,
    CONTENT_SAVE_FAILED,
    CONTENT_ROLLBACK_COMPLETED,

    // Analytics 관련 이벤트
    ANALYTICS_UPDATED,
    ANALYTICS_UPDATE_FAILED,
    ANALYTICS_ROLLBACK_COMPLETED
}