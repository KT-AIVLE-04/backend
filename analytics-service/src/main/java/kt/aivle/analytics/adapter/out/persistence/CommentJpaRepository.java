package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.Comment;

@Repository
public interface CommentJpaRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByUserIdAndSocialPostIdOrderByCrawledAtDesc(String userId, Long socialPostId);
    
    List<Comment> findByUserIdAndCrawledAtBetweenOrderByCrawledAtDesc(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Comment> findBySocialPostId(Long socialPostId);
    
    void deleteByUserId(String userId);
}
