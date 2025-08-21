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
     * 날짜 범위 유효성 검증
     */
    void validateDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 사용자 접근 권한 검증
     */
    void validateUserAccess(Long userId, Long accountId);
    
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
     * 메트릭 데이터 DTO
     */
    class MetricsData {
        private final Long subscriberCount;
        private final Long viewCount;
        private final Long likeCount;
        private final Long commentCount;
        private final String type;
        private final Long id;
        
        public MetricsData(Long subscriberCount, Long viewCount, Long likeCount, 
                          Long commentCount, String type, Long id) {
            this.subscriberCount = subscriberCount;
            this.viewCount = viewCount;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.type = type;
            this.id = id;
        }
        
        public Long getSubscriberCount() { return subscriberCount; }
        public Long getViewCount() { return viewCount; }
        public Long getLikeCount() { return likeCount; }
        public Long getCommentCount() { return commentCount; }
        public String getType() { return type; }
        public Long getId() { return id; }
    }
}
