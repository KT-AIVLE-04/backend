package kt.aivle.sns.exception;

import kt.aivle.common.code.DefaultCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum SnsErrorCode implements DefaultCode {

    NOT_FOUND_POST(HttpStatus.NOT_FOUND, false, "게시글을 찾을 수 없습니다."),
    NOT_AUTHORIZED_CONTENT(HttpStatus.UNAUTHORIZED, false, "컨텐츠에 접근할 권한이 없습니다."),
    IMAGE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, false, "이미지 업로드에 실패했습니다."),
    FAIL_DECODE_IMAGE(HttpStatus.BAD_REQUEST, false, "이미지 디코딩에 실패했습니다."),
    THUMBNAIL_GENERATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "썸네일 생성에 실패했습니다."),
    FAIL_GET_METADATA(HttpStatus.INTERNAL_SERVER_ERROR, false, "메타데이터 조회에 실패했습니다."),
    CLOUD_FRONT_SIGN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "CloudFront URL 서명에 실패했습니다."),

    FAIL_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, false, "업로드에 실패했습니다."),

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
