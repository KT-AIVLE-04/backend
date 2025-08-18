package kt.aivle.analytics.exception;

import lombok.Getter;

@Getter
public class AnalyticsException extends RuntimeException {
    
    private final AnalyticsErrorCode errorCode;
    
    public AnalyticsException(String message) {
        super(message);
        this.errorCode = AnalyticsErrorCode.INTERNAL_ERROR;
    }
    
    public AnalyticsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = AnalyticsErrorCode.INTERNAL_ERROR;
    }
    
    public AnalyticsException(AnalyticsErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public AnalyticsException(AnalyticsErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public AnalyticsException(AnalyticsErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
