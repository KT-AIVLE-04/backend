package kt.aivle.analytics.application.port.out;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsAccountMetric;

public interface SnsAccountMetricRepositoryPort {
    SnsAccountMetric save(SnsAccountMetric snsAccountMetric);
    Optional<SnsAccountMetric> findById(Long id);
    List<SnsAccountMetric> findAll();
    List<SnsAccountMetric> findByAccountId(Long accountId);
    List<SnsAccountMetric> findByAccountIdAndCreatedAtBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate);
    List<SnsAccountMetric> findByAccountIdAndCreatedAtDate(Long accountId, Date date);
    boolean existsByAccountIdAndCreatedAtAfter(Long accountId, LocalDateTime date);
    void deleteById(Long id);
}
