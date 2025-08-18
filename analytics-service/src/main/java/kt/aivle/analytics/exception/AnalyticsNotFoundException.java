package kt.aivle.analytics.exception;

public class AnalyticsNotFoundException extends AnalyticsException {
    
    public AnalyticsNotFoundException(String message) {
        super(AnalyticsErrorCode.NO_DATA_AVAILABLE, message);
    }
    
    public AnalyticsNotFoundException(String message, Throwable cause) {
        super(AnalyticsErrorCode.NO_DATA_AVAILABLE, message, cause);
    }
    
    public static AnalyticsNotFoundException postNotFound(String postId) {
        return new AnalyticsNotFoundException("Post not found with ID: " + postId);
    }
    
    public static AnalyticsNotFoundException accountNotFound(String accountId) {
        return new AnalyticsNotFoundException("Account not found with ID: " + accountId);
    }
    
    public static AnalyticsNotFoundException userNotFound(String userId) {
        return new AnalyticsNotFoundException("User not found with ID: " + userId);
    }
}
