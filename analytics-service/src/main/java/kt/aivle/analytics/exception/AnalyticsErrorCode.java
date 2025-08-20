package kt.aivle.analytics.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnalyticsErrorCode {
    
    // 입력 검증 관련
    INVALID_POST_ID("ANALYTICS-001", "Invalid post ID format"),
    INVALID_ACCOUNT_ID("ANALYTICS-002", "Invalid account ID format"),
    INVALID_DATE("ANALYTICS-003", "Invalid date parameter"),
    INVALID_PAGINATION("ANALYTICS-004", "Invalid pagination parameters"),
    
    // 데이터 조회 관련
    POST_NOT_FOUND("ANALYTICS-101", "Post not found"),
    ACCOUNT_NOT_FOUND("ANALYTICS-102", "Account not found"),
    NO_DATA_AVAILABLE("ANALYTICS-103", "No data available for the specified criteria"),
    
    // YouTube API 관련
    YOUTUBE_API_ERROR("ANALYTICS-201", "YouTube API error occurred"),
    YOUTUBE_QUOTA_EXCEEDED("ANALYTICS-202", "YouTube API quota exceeded"),
    YOUTUBE_VIDEO_NOT_FOUND("ANALYTICS-203", "YouTube video not found"),
    YOUTUBE_CHANNEL_NOT_FOUND("ANALYTICS-204", "YouTube channel not found"),
    
    // AI 분석 관련
    AI_ANALYSIS_ERROR("ANALYTICS-301", "AI analysis service error occurred"),
    EMOTION_ANALYSIS_ERROR("ANALYTICS-302", "Emotion analysis failed"),
    
    // 권한 관련
    UNAUTHORIZED_ACCESS("ANALYTICS-401", "Unauthorized access to data"),
    USER_MISMATCH("ANALYTICS-402", "User ID mismatch"),
    
    // 시스템 관련
    INTERNAL_ERROR("ANALYTICS-500", "Internal server error"),
    DATABASE_ERROR("ANALYTICS-501", "Database operation failed"),
    CACHE_ERROR("ANALYTICS-502", "Cache operation failed");
    
    private final String code;
    private final String message;
}
