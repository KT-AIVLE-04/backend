package kt.aivle.analytics.application.port.out;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsAccountMetric;

public interface SnsAccountMetricRepositoryPort {
    SnsAccountMetric save(SnsAccountMetric snsAccountMetric);
    Optional<SnsAccountMetric> findById(Long id);
    List<SnsAccountMetric> findAll();
    List<SnsAccountMetric> findByAccountId(Long accountId);
    List<SnsAccountMetric> findByAccountIdAndCreatedAtDate(Long accountId, Date date);
    boolean existsByAccountIdAndCreatedAtAfter(Long accountId, Date date);
    void deleteById(Long id);
}
