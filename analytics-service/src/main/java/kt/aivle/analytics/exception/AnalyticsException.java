package kt.aivle.analytics.exception;

public class AnalyticsException extends RuntimeException {
    
    public AnalyticsException(String message) {
        super(message);
    }
    
    public AnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AnalyticsException(Throwable cause) {
        super(cause);
    }
}
