package kt.aivle.analytics.application.port.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsAccountMetric;

public interface SnsAccountMetricRepositoryPort {
    SnsAccountMetric save(SnsAccountMetric snsAccountMetric);
    Optional<SnsAccountMetric> findById(Long id);
    List<SnsAccountMetric> findAll();
    List<SnsAccountMetric> findByAccountId(Long accountId);
    List<SnsAccountMetric> findByAccountIdAndCreatedAtDate(Long accountId, LocalDate date);
    boolean existsByAccountIdAndCreatedAtAfter(Long accountId, LocalDateTime date);
    void deleteById(Long id);
}
