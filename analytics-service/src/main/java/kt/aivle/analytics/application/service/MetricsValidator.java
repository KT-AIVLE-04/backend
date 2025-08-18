package kt.aivle.analytics.application.service;

import org.springframework.stereotype.Component;

import kt.aivle.analytics.exception.AnalyticsException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MetricsValidator {
    
    private static final long MAX_REASONABLE_SUBSCRIBERS = 100_000_000L; // 1억 구독자
    private static final long MAX_REASONABLE_VIEWS = 1_000_000_000L; // 10억 조회수
    private static final long MAX_REASONABLE_LIKES = 10_000_000L; // 1000만 좋아요
    private static final long MAX_REASONABLE_COMMENTS = 10_000_000L; // 1000만 댓글
    
    /**
     * 메트릭 값의 유효성을 검증합니다.
     */
    public boolean isValidMetric(Long value, long maxValue) {
        return value != null && value >= 0 && value <= maxValue;
    }
    
    /**
     * 구독자 수를 검증합니다.
     */
    public void validateSubscriberCount(Long count, Long accountId) {
        if (!isValidMetric(count, MAX_REASONABLE_SUBSCRIBERS)) {
            log.warn("Invalid subscriber count for accountId: {}, value: {}", accountId, count);
            throw new AnalyticsException("Invalid subscriber count for accountId: " + accountId);
        }
    }
    
    /**
     * 조회수를 검증합니다.
     */
    public void validateViewCount(Long count, Long id, String type) {
        if (!isValidMetric(count, MAX_REASONABLE_VIEWS)) {
            log.warn("Invalid view count for {}: {}, value: {}", type, id, count);
            throw new AnalyticsException("Invalid view count for " + type + ": " + id);
        }
    }
    
    /**
     * 좋아요 수를 검증합니다.
     */
    public void validateLikeCount(Long count, Long id) {
        if (!isValidMetric(count, MAX_REASONABLE_LIKES)) {
            log.warn("Invalid like count for postId: {}, value: {}", id, count);
            throw new AnalyticsException("Invalid like count for postId: " + id);
        }
    }
    
    /**
     * 댓글 수를 검증합니다.
     */
    public void validateCommentCount(Long count, Long id) {
        if (!isValidMetric(count, MAX_REASONABLE_COMMENTS)) {
            log.warn("Invalid comment count for postId: {}, value: {}", id, count);
            throw new AnalyticsException("Invalid comment count for postId: " + id);
        }
    }
}
