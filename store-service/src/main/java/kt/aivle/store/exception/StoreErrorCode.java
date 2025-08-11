package kt.aivle.store.exception;

import kt.aivle.common.code.DefaultCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum StoreErrorCode implements DefaultCode {

    NOT_FOUND_STORE(HttpStatus.NOT_FOUND, false, "매장을 찾을 수 없습니다."),
    NOT_AUTHORITY(HttpStatus.FORBIDDEN, false, "권한이 없습니다."),
    NOT_FOUND_INDUSTRY(HttpStatus.NOT_FOUND, false, "해당 업종을 찾을 수 없습니다."),
    KAFKA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "카프카 통신에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final boolean success;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
