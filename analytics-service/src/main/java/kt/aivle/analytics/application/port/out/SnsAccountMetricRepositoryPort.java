package kt.aivle.analytics.application.port.out;

import kt.aivle.analytics.domain.entity.SnsAccountMetric;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SnsAccountMetricRepositoryPort {
    SnsAccountMetric save(SnsAccountMetric snsAccountMetric);
    Optional<SnsAccountMetric> findById(Long id);
    List<SnsAccountMetric> findAll();
    List<SnsAccountMetric> findByAccountId(Long accountId);
    List<SnsAccountMetric> findByAccountIdAndCreatedAtBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByAccountIdAndCreatedAtAfter(Long accountId, LocalDateTime date);
    void deleteById(Long id);
}
