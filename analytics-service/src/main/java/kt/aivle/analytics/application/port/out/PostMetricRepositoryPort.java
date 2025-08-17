package kt.aivle.analytics.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.PostMetric;

public interface PostMetricRepositoryPort {
    
    PostMetric save(PostMetric postMetric);
    
    Optional<PostMetric> findById(Long id);
    
    List<PostMetric> findByPostId(Long postId);
    
    List<PostMetric> findByPostIdAndCrawledAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<PostMetric> findByAccountIdAndCrawledAtBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate);
    
    Optional<PostMetric> findLatestByPostId(Long postId);
    
    void deleteByPostId(Long postId);
}
