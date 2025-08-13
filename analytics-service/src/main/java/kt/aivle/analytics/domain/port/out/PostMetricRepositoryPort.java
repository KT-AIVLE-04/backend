package kt.aivle.analytics.domain.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.PostMetric;

public interface PostMetricRepositoryPort {
    
    List<PostMetric> findByUserIdAndSocialPostIdAndDateRange(String userId, Long socialPostId, LocalDate startDate, LocalDate endDate);
    
    List<PostMetric> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate);
    
    List<PostMetric> findTopPerformingByUserId(String userId, int limit);
    
    Optional<PostMetric> findBySocialPostIdAndMetricDate(Long socialPostId, LocalDate metricDate);
    
    void deleteByUserId(String userId);
    
    PostMetric save(PostMetric postMetric);
    
    List<PostMetric> saveAll(List<PostMetric> postMetrics);
}
