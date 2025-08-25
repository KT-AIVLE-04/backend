package kt.aivle.analytics.application.port.out.infrastructure;

import java.time.LocalDate;



/**
 * 비즈니스 규칙 검증을 위한 Port 인터페이스
 * 도메인 서비스의 검증 로직을 추상화
 */
public interface ValidationPort {
    
    /**
     * 메트릭 데이터 유효성 검증
     */
    void validateMetrics(MetricsData metricsData);
    /**
     * 구독자 수 유효성 검증
     */
    void validateSubscriberCount(Long count, Long accountId);
    
    /**
     * 조회수 유효성 검증
     */
    void validateViewCount(Long count, Long id, String type);
    
    /**
     * 좋아요 수 유효성 검증
     */
    void validateLikeCount(Long count, Long id);
    
    /**
     * 댓글 수 유효성 검증
     */
    void validateCommentCount(Long count, Long id);


    
    /**
     * 날짜 문자열 유효성 검증 및 파싱
     */
    LocalDate validateAndParseDate(String dateStr);
    
    /**
     * 사용자 ID 유효성 검증
     */
    void validateUserId(String userId);
    
    /**
     * 게시물 ID 유효성 검증
     */
    void validatePostId(String postId);
    
    /**
     * 계정 ID 유효성 검증
     */
    void validateAccountId(Long accountId);

    /**
     * 메트릭 데이터 DTO
     */
    record MetricsData(Long subscriberCount, Long viewCount, Long likeCount, Long commentCount, String type, Long id) {
    }
}
