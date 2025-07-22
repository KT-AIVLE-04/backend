package kt.aivle.common.exception;

import kt.aivle.common.code.DefaultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final DefaultCode code;

    public BusinessException(DefaultCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public BusinessException(DefaultCode code, Throwable cause) {
        super(code.getMessage(), cause);
        this.code = code;
    }

    public BusinessException(DefaultCode code, String customMessage) {
        super(customMessage);
        this.code = code;
    }
}
