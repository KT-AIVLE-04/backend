package kt.aivle.common.code;

import org.springframework.http.HttpStatus;

public interface DefaultCode {
    HttpStatus getHttpStatus();
    boolean isSuccess();
    String name();
    String getMessage();
}
