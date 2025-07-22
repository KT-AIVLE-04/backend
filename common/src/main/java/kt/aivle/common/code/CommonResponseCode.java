package kt.aivle.common.code;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum CommonResponseCode implements DefaultCode {
    // 성공
    OK(HttpStatus.OK, true, "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, true, "자원이 성공적으로 생성되었습니다."),
    ACCEPTED(HttpStatus.ACCEPTED, true, "요청이 수락되었습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, true, "내용 없음"),

    // 실패
    BAD_REQUEST(HttpStatus.BAD_REQUEST, false, "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, false,"요청한 리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, false, "요청이 충돌했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "서버 내부 오류입니다.");

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
