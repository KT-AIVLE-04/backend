package kt.aivle.analytics.adapter.out.persistence.repository;

import kt.aivle.analytics.domain.entity.PostCommentMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostCommentMetricJpaRepository extends JpaRepository<PostCommentMetric, Long> {
    
    List<PostCommentMetric> findByPostId(Long postId);
    
    List<PostCommentMetric> findByPostIdAndCrawledAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate);
    
    Optional<PostCommentMetric> findBySnsCommentId(String snsCommentId);
    
    void deleteByPostId(Long postId);
    
    void deleteBySnsCommentId(String snsCommentId);
}
