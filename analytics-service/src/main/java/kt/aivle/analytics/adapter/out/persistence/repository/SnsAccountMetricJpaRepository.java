package kt.aivle.analytics.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.SnsAccountMetric;

@Repository
public interface SnsAccountMetricJpaRepository extends BaseJpaRepository<SnsAccountMetric, Long> {
    List<SnsAccountMetric> findByAccountId(Long accountId);
    List<SnsAccountMetric> findByAccountIdAndCreatedAtBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByAccountIdAndCreatedAtAfter(Long accountId, LocalDateTime date);
}
