package kt.aivle.common.exception;

import kt.aivle.common.code.DefaultCode;
import lombok.Getter;

@Getter
public class InfraException extends RuntimeException {
    private final DefaultCode code;

    public InfraException(DefaultCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public InfraException(DefaultCode code, Throwable cause) {
        super(code.getMessage(), cause);
        this.code = code;
    }

    public InfraException(DefaultCode code, String customMessage) {
        super(customMessage);
        this.code = code;
    }
}
