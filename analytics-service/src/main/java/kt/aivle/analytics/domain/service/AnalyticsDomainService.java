package kt.aivle.analytics.domain.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import kt.aivle.analytics.application.port.out.infrastructure.ValidationPort;
import kt.aivle.analytics.exception.AnalyticsException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AnalyticsDomainService implements ValidationPort {
    
    private static final long MAX_REASONABLE_SUBSCRIBERS = 100_000_000L; // 1억 구독자
    private static final long MAX_REASONABLE_VIEWS = 1_000_000_000L; // 10억 조회수
    private static final long MAX_REASONABLE_LIKES = 10_000_000L; // 1000만 좋아요
    private static final long MAX_REASONABLE_COMMENTS = 10_000_000L; // 1000만 댓글
    
    @Override
    public void validateMetrics(MetricsData metricsData) {
        if (metricsData.getSubscriberCount() != null) {
            validateSubscriberCount(metricsData.getSubscriberCount(), metricsData.getId());
        }
        
        if (metricsData.getViewCount() != null) {
            validateViewCount(metricsData.getViewCount(), metricsData.getId(), metricsData.getType());
        }
        
        if (metricsData.getLikeCount() != null) {
            validateLikeCount(metricsData.getLikeCount(), metricsData.getId());
        }
        
        if (metricsData.getCommentCount() != null) {
            validateCommentCount(metricsData.getCommentCount(), metricsData.getId());
        }
    }
    
    @Override
    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new AnalyticsException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new AnalyticsException("Start date cannot be after end date");
        }
        
        if (startDate.isAfter(LocalDate.now())) {
            throw new AnalyticsException("Start date cannot be in the future");
        }
        
        if (endDate.isAfter(LocalDate.now())) {
            throw new AnalyticsException("End date cannot be in the future");
        }
        
        // 최대 1년 범위 제한
        if (startDate.plusYears(1).isBefore(endDate)) {
            throw new AnalyticsException("Date range cannot exceed 1 year");
        }
    }
    
    @Override
    public void validateUserAccess(Long userId, Long accountId) {
        if (userId == null || accountId == null) {
            throw new AnalyticsException("User ID and Account ID cannot be null");
        }
        
        if (userId <= 0 || accountId <= 0) {
            throw new AnalyticsException("User ID and Account ID must be positive");
        }
        
        // 실제 구현에서는 사용자가 해당 계정에 접근 권한이 있는지 확인
        // 예: 데이터베이스에서 사용자-계정 관계 조회
        log.debug("Validating user access: userId={}, accountId={}", userId, accountId);
    }
    
    @Override
    public void validateSubscriberCount(Long count, Long accountId) {
        if (!isValidMetric(count, MAX_REASONABLE_SUBSCRIBERS)) {
            log.warn("Invalid subscriber count for accountId: {}, value: {}", accountId, count);
            throw new AnalyticsException("Invalid subscriber count for accountId: " + accountId);
        }
    }
    
    @Override
    public void validateViewCount(Long count, Long id, String type) {
        if (!isValidMetric(count, MAX_REASONABLE_VIEWS)) {
            log.warn("Invalid view count for {}: {}, value: {}", type, id, count);
            throw new AnalyticsException("Invalid view count for " + type + ": " + id);
        }
    }
    
    @Override
    public void validateLikeCount(Long count, Long id) {
        if (!isValidMetric(count, MAX_REASONABLE_LIKES)) {
            log.warn("Invalid like count for postId: {}, value: {}", id, count);
            throw new AnalyticsException("Invalid like count for postId: " + id);
        }
    }
    
    @Override
    public void validateCommentCount(Long count, Long id) {
        if (!isValidMetric(count, MAX_REASONABLE_COMMENTS)) {
            log.warn("Invalid comment count for postId: {}, value: {}", id, count);
            throw new AnalyticsException("Invalid comment count for postId: " + id);
        }
    }
    
    /**
     * 메트릭 값의 유효성을 검증합니다.
     */
    private boolean isValidMetric(Long value, long maxValue) {
        return value != null && value >= 0 && value <= maxValue;
    }
}
