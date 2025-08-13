package kt.aivle.analytics.exception;

import org.springframework.http.HttpStatus;

import kt.aivle.common.code.DefaultCode;

public enum AnalyticsErrorCode implements DefaultCode {
    
    // 토큰 관련 에러
    TOKEN_NOT_FOUND("ANALYTICS_001", "토큰을 찾을 수 없습니다."),
    TOKEN_EXPIRED("ANALYTICS_002", "토큰이 만료되었습니다."),
    TOKEN_REFRESH_FAILED("ANALYTICS_003", "토큰 갱신에 실패했습니다."),
    
    // YouTube API 관련 에러
    YOUTUBE_API_ERROR("ANALYTICS_101", "YouTube API 호출에 실패했습니다."),
    YOUTUBE_CHANNEL_NOT_FOUND("ANALYTICS_102", "YouTube 채널을 찾을 수 없습니다."),
    YOUTUBE_VIDEO_NOT_FOUND("ANALYTICS_103", "YouTube 비디오를 찾을 수 없습니다."),
    
    // AI 서버 관련 에러
    AI_SERVER_ERROR("ANALYTICS_201", "AI 서버 호출에 실패했습니다."),
    SENTIMENT_ANALYSIS_FAILED("ANALYTICS_202", "감정 분석에 실패했습니다."),
    TREND_ANALYSIS_FAILED("ANALYTICS_203", "트렌드 분석에 실패했습니다."),
    
    // 메트릭 관련 에러
    METRICS_COLLECTION_FAILED("ANALYTICS_301", "메트릭 수집에 실패했습니다."),
    INVALID_DATE_RANGE("ANALYTICS_302", "유효하지 않은 날짜 범위입니다."),
    
    // 기타 에러
    UNSUPPORTED_SNS_TYPE("ANALYTICS_401", "지원하지 않는 SNS 타입입니다."),
    
    // Kafka 관련 에러
    KAFKA_ERROR("ANALYTICS_501", "Kafka 이벤트 처리에 실패했습니다.");
    
    private final String code;
    private final String message;
    
    AnalyticsErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    
    @Override
    public boolean isSuccess() {
        return false;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
    
    public String getCode() {
        return code;
    }
}
