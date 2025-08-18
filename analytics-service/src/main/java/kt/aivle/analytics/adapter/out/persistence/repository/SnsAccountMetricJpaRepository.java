package kt.aivle.analytics.adapter.out.persistence.repository;

import kt.aivle.analytics.domain.entity.SnsAccountMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SnsAccountMetricJpaRepository extends JpaRepository<SnsAccountMetric, Long> {
    List<SnsAccountMetric> findByAccountId(Long accountId);
    List<SnsAccountMetric> findByAccountIdAndCrawledAtBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByAccountIdAndCrawledAtAfter(Long accountId, LocalDateTime date);
}
