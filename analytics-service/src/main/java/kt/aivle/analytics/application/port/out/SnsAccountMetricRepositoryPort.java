package kt.aivle.analytics.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsAccountMetric;

public interface SnsAccountMetricRepositoryPort {
    SnsAccountMetric save(SnsAccountMetric snsAccountMetric);
    Optional<SnsAccountMetric> findById(Long id);
    List<SnsAccountMetric> findAll();
    List<SnsAccountMetric> findByAccountId(Long accountId);
    List<SnsAccountMetric> findByAccountIdAndCreatedAtDate(Long accountId, LocalDate date);
    boolean existsByAccountIdAndCreatedAtAfter(Long accountId, LocalDate date);
    void deleteById(Long id);
}
