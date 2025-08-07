package kt.aivle.shorts.exception;

import kt.aivle.common.code.DefaultCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ShortsErrorCode implements DefaultCode {

    NOT_GET_STORE(HttpStatus.NOT_FOUND, false, "매장 정보를 불러오지 못했습니다."),
    CONTENTS_EVENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "콘텐츠 이벤트 발행에 실패했습니다."),
    KAFKA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "카프카 통신에 실패했습니다."),
    IMAGE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, false, "이미지 업로드에 실패했습니다. (지원 형식: JPEG, PNG)"),
    AI_WEB_CLIENT_ERROR(HttpStatus.BAD_REQUEST, false, "AI 웹 클라이언트 통신에 실패했습니다.");

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
