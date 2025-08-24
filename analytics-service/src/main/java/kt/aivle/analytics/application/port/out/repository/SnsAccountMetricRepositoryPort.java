package kt.aivle.analytics.application.port.out.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsAccountMetric;

public interface SnsAccountMetricRepositoryPort {
    SnsAccountMetric save(SnsAccountMetric snsAccountMetric);
    Optional<SnsAccountMetric> findById(Long id);

    List<SnsAccountMetric> findByAccountIdAndCreatedAtDate(Long accountId, LocalDate date);
    
    /**
     * Account Metrics를 Account 정보와 함께 조회 (JOIN 쿼리)
     */
    List<Object[]> findMetricsWithAccount(List<Long> accountIds, LocalDate date);
    
    boolean existsByAccountIdAndCreatedAtAfter(Long accountId, LocalDateTime date);
    void deleteById(Long id);
}
