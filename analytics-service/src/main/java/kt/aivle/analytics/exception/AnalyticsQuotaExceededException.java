package kt.aivle.analytics.exception;

public class AnalyticsQuotaExceededException extends AnalyticsException {
    
    public AnalyticsQuotaExceededException(String message) {
        super(AnalyticsErrorCode.YOUTUBE_QUOTA_EXCEEDED, message);
    }
    
    public AnalyticsQuotaExceededException(String message, Throwable cause) {
        super(AnalyticsErrorCode.YOUTUBE_QUOTA_EXCEEDED, message, cause);
    }
    
    public static AnalyticsQuotaExceededException quotaExceeded(int currentUsage, int limit) {
        return new AnalyticsQuotaExceededException(
            String.format("YouTube API quota exceeded. Current usage: %d, Limit: %d", currentUsage, limit)
        );
    }
}
