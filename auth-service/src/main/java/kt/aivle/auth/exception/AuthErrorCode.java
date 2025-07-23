package kt.aivle.auth.exception;

import kt.aivle.common.code.DefaultCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum AuthErrorCode implements DefaultCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, false, "이미 가입된 이메일입니다."),
    INVALID_PASSWORD_POLICY(HttpStatus.BAD_REQUEST, false, "비밀번호는 2종류(영문, 숫자, 특수문자) 이상 10자리, 3종류 8자리 이상이어야 하며, 개인정보/연속문자/이메일 등과 유사하거나 동일하지 않아야 합니다.");

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
