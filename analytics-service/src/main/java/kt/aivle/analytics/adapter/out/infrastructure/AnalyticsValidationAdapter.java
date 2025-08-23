package kt.aivle.analytics.adapter.out.infrastructure;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import kt.aivle.analytics.application.port.out.infrastructure.ValidationPort;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AnalyticsValidationAdapter implements ValidationPort {
    
    private static final long MAX_REASONABLE_SUBSCRIBERS = 100_000_000L; // 1억 구독자
    private static final long MAX_REASONABLE_VIEWS = 1_000_000_000L; // 10억 조회수
    private static final long MAX_REASONABLE_LIKES = 10_000_000L; // 1000만 좋아요
    private static final long MAX_REASONABLE_COMMENTS = 10_000_000L; // 1000만 댓글
    
    @Override
    public void validateMetrics(MetricsData metricsData) {
        if (metricsData.subscriberCount() != null) {
            validateSubscriberCount(metricsData.subscriberCount(), metricsData.id());
        }
        
        if (metricsData.viewCount() != null) {
            validateViewCount(metricsData.viewCount(), metricsData.id(), metricsData.type());
        }
        
        if (metricsData.likeCount() != null) {
            validateLikeCount(metricsData.likeCount(), metricsData.id());
        }
        
        if (metricsData.commentCount() != null) {
            validateCommentCount(metricsData.commentCount(), metricsData.id());
        }
    }

    
    @Override
    public void validateSubscriberCount(Long count, Long accountId) {
        if (!isValidMetric(count, MAX_REASONABLE_SUBSCRIBERS)) {
            log.warn("Invalid subscriber count for accountId: {}, value: {}", accountId, count);
            throw new BusinessException(AnalyticsErrorCode.INVALID_ACCOUNT_ID);
        }
    }
    
    @Override
    public void validateViewCount(Long count, Long id, String type) {
        if (!isValidMetric(count, MAX_REASONABLE_VIEWS)) {
            log.warn("Invalid view count for {}: {}, value: {}", type, id, count);
            throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
        }
    }
    
    @Override
    public void validateLikeCount(Long count, Long id) {
        if (!isValidMetric(count, MAX_REASONABLE_LIKES)) {
            log.warn("Invalid like count for postId: {}, value: {}", id, count);
            throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
        }
    }
    
    @Override
    public void validateCommentCount(Long count, Long id) {
        if (!isValidMetric(count, MAX_REASONABLE_COMMENTS)) {
            log.warn("Invalid comment count for postId: {}, value: {}", id, count);
            throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
        }
    }
    
    /**
     * 메트릭 값의 유효성을 검증합니다.
     */
    private boolean isValidMetric(Long value, long maxValue) {
        return value != null && value >= 0 && value <= maxValue;
    }
}
