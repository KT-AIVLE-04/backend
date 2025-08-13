package kt.aivle.snspost.exception;

import kt.aivle.common.code.DefaultCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum SnsPostErrorCode implements DefaultCode {
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, false, "게시물을 찾을 수 없습니다."),
    POST_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, "게시물 생성에 실패했습니다."),
    HASHTAG_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, "해시태그 생성에 실패했습니다."),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, false, "유효하지 않은 콘텐츠 타입입니다."),
    INVALID_SNS_PLATFORM(HttpStatus.BAD_REQUEST, false, "유효하지 않은 SNS 플랫폼입니다."),
    FASTAPI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, false, "AI 서비스가 일시적으로 사용할 수 없습니다.");

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