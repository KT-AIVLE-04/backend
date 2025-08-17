package kt.aivle.analytics.application.port.out;

import kt.aivle.analytics.domain.entity.PostCommentMetric;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostCommentMetricRepositoryPort {
    
    PostCommentMetric save(PostCommentMetric postCommentMetric);
    
    Optional<PostCommentMetric> findById(Long id);
    
    List<PostCommentMetric> findByPostId(Long postId);
    
    List<PostCommentMetric> findByPostIdAndCrawledAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate);
    
    Optional<PostCommentMetric> findBySnsCommentId(String snsCommentId);
    
    void deleteByPostId(Long postId);
    
    void deleteBySnsCommentId(String snsCommentId);
}
