package kt.aivle.auth.exception;

import org.springframework.http.HttpStatus;

import kt.aivle.common.code.DefaultCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AuthErrorCode implements DefaultCode {

    NOT_FOUND_USER(HttpStatus.NOT_FOUND, false, "사용자를 찾을 수 없습니다."),

    // 회원가입
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, false, "이미 가입된 이메일입니다."),
    INVALID_PASSWORD_POLICY(HttpStatus.BAD_REQUEST, false, "비밀번호는 2종류(영문, 숫자, 특수문자) 이상 10자리, 3종류 8자리 이상이어야 하며, 개인정보/연속문자/이메일 등과 유사하거나 동일하지 않아야 합니다."),

    // 로그인
    NOT_FOUND_EMAIL(HttpStatus.NOT_FOUND, false, "사용자를 찾을 수 없습니다."),
    NOT_MATCHES_PASSWORD(HttpStatus.UNAUTHORIZED, false, "비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_EMAIL(HttpStatus.UNAUTHORIZED, false, "계정이 잠금되었습니다."),

    // 재발급
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, false, "유효하지 않은 재발급 토큰입니다."),

    // OAuth2
    INVALID_REDIRECT_URL(HttpStatus.BAD_REQUEST, false, "유효하지 않은 리다이렉트 URL입니다.");


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
