package kt.aivle.analytics.adapter.out.persistence.repository;

import kt.aivle.analytics.domain.entity.SnsPostMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SnsPostMetricJpaRepository extends JpaRepository<SnsPostMetric, Long> {
    List<SnsPostMetric> findByPostId(Long postId);
    List<SnsPostMetric> findByPostIdAndCreatedAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByPostIdAndCreatedAtAfter(Long postId, LocalDateTime date);
}
