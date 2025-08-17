package kt.aivle.analytics.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.PostMetric;

@Repository
public interface PostMetricJpaRepository extends JpaRepository<PostMetric, Long> {
    
    List<PostMetric> findByPostId(Long postId);
    
    List<PostMetric> findByPostIdAndCrawledAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT pm FROM PostMetric pm JOIN Post p ON pm.postId = p.id WHERE p.accountId = :accountId AND pm.crawledAt BETWEEN :startDate AND :endDate")
    List<PostMetric> findByAccountIdAndCrawledAtBetween(@Param("accountId") Long accountId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT pm FROM PostMetric pm WHERE pm.postId = :postId ORDER BY pm.crawledAt DESC LIMIT 1")
    Optional<PostMetric> findLatestByPostId(@Param("postId") Long postId);
    
    void deleteByPostId(Long postId);
}
