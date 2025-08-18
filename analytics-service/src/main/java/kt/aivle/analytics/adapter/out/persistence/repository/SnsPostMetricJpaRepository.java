package kt.aivle.analytics.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.SnsPostMetric;

@Repository
public interface SnsPostMetricJpaRepository extends BaseJpaRepository<SnsPostMetric, Long> {
    List<SnsPostMetric> findByPostId(Long postId);
    List<SnsPostMetric> findByPostIdAndCreatedAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByPostIdAndCreatedAtAfter(Long postId, LocalDateTime date);
}
