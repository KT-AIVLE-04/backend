package kt.aivle.analytics.exception;

public class AnalyticsValidationException extends AnalyticsException {
    
    public AnalyticsValidationException(String message) {
        super(AnalyticsErrorCode.INVALID_POST_ID, message);
    }
    
    public AnalyticsValidationException(String message, Throwable cause) {
        super(AnalyticsErrorCode.INVALID_POST_ID, message, cause);
    }
    
    public static AnalyticsValidationException invalidPostId(String postId) {
        return new AnalyticsValidationException("Invalid post ID format: " + postId);
    }
    
    public static AnalyticsValidationException invalidAccountId(String accountId) {
        return new AnalyticsValidationException("Invalid account ID format: " + accountId);
    }
    
    public static AnalyticsValidationException invalidUserId(String userId) {
        return new AnalyticsValidationException("Invalid user ID format: " + userId);
    }
    
    public static AnalyticsValidationException invalidPagination(Integer page, Integer size) {
        return new AnalyticsValidationException("Invalid pagination parameters - page: " + page + ", size: " + size);
    }
}
