package kt.aivle.analytics.exception;

import org.springframework.http.HttpStatus;

import kt.aivle.common.code.DefaultCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AnalyticsErrorCode implements DefaultCode {

    // 입력 검증 관련
    INVALID_POST_ID(HttpStatus.BAD_REQUEST, false, "잘못된 게시물 ID 형식입니다."),
    INVALID_ACCOUNT_ID(HttpStatus.BAD_REQUEST, false, "잘못된 계정입니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, false, "잘못된 사용자 ID 형식입니다."),
    INVALID_SNS_TYPE(HttpStatus.BAD_REQUEST, false, "잘못된 SNS 타입입니다."),
    INVALID_DATE(HttpStatus.BAD_REQUEST, false, "잘못된 날짜 매개변수입니다."),
    INVALID_PAGINATION(HttpStatus.BAD_REQUEST, false, "잘못된 페이지네이션 매개변수입니다."),
    
    // 데이터 조회 관련
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, false, "게시물을 찾을 수 없습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, false, "계정을 찾을 수 없습니다."),
    NO_DATA_AVAILABLE(HttpStatus.NOT_FOUND, false, "지정된 조건에 해당하는 데이터가 없습니다."),
    
    // YouTube API 관련
    YOUTUBE_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "YouTube API 오류가 발생했습니다."),
    YOUTUBE_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, false, "YouTube API 할당량이 초과되었습니다."),
    YOUTUBE_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, false, "YouTube 비디오를 찾을 수 없습니다."),
    YOUTUBE_CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, false, "YouTube 채널을 찾을 수 없습니다."),
    
    // AI 분석 관련
    AI_ANALYSIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "AI 분석 서비스 오류가 발생했습니다."),
    EMOTION_ANALYSIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "감정 분석에 실패했습니다."),
    
    // 배치 작업 관련
    BATCH_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, false, "배치 작업을 찾을 수 없습니다."),
    BATCH_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, "배치 작업 실행에 실패했습니다."),
    
    // 외부 API 관련
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "외부 API 호출에 실패했습니다."),
    API_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, false, "API 호출 한도를 초과했습니다."),
    
    // 권한 관련
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, false, "데이터에 대한 권한이 없습니다."),
    USER_MISMATCH(HttpStatus.FORBIDDEN, false, "사용자 ID가 일치하지 않습니다."),
    
    // 시스템 관련
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "내부 서버 오류가 발생했습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "데이터베이스 작업에 실패했습니다."),
    CACHE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, "캐시 작업에 실패했습니다.");

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
