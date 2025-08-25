package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.SnsAccountMetricJpaRepository;
import kt.aivle.analytics.application.port.out.repository.SnsAccountMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsAccountMetric;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SnsAccountMetricRepository implements SnsAccountMetricRepositoryPort {

    private final SnsAccountMetricJpaRepository snsAccountMetricJpaRepository;

    @Override
    public SnsAccountMetric save(SnsAccountMetric snsAccountMetric) {
        return snsAccountMetricJpaRepository.save(snsAccountMetric);
    }

    @Override
    public Optional<SnsAccountMetric> findById(Long id) {
        return snsAccountMetricJpaRepository.findById(id);
    }

    @Override
    public List<SnsAccountMetric> findByAccountIdAndCreatedAtDate(Long accountId, LocalDate date) {
        // LocalDate를 direct 사용 (JpaRepository에서 타임존 처리)
        return snsAccountMetricJpaRepository.findByAccountIdAndCreatedAtDate(accountId, date);
    }

    @Override
    public boolean existsByAccountIdAndCreatedAtAfter(Long accountId, LocalDateTime date) {
        // LocalDateTime을 직접 사용
        return snsAccountMetricJpaRepository.existsByAccountIdAndCreatedAtAfter(accountId, date);
    }

    @Override
    public void deleteById(Long id) {
        snsAccountMetricJpaRepository.deleteById(id);
    }
    
    @Override
    public List<Object[]> findMetricsWithAccount(List<Long> accountIds, LocalDate date) {
        return snsAccountMetricJpaRepository.findMetricsWithAccount(accountIds, date);
    }
}
