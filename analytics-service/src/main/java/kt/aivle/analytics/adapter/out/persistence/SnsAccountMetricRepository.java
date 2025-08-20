package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.SnsAccountMetricJpaRepository;
import kt.aivle.analytics.application.port.out.SnsAccountMetricRepositoryPort;
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
    public List<SnsAccountMetric> findAll() {
        return snsAccountMetricJpaRepository.findAll();
    }

    @Override
    public List<SnsAccountMetric> findByAccountId(Long accountId) {
        return snsAccountMetricJpaRepository.findByAccountId(accountId);
    }

    @Override
    public List<SnsAccountMetric> findByAccountIdAndCreatedAtBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return snsAccountMetricJpaRepository.findByAccountIdAndCreatedAtBetween(accountId, startDate, endDate);
    }

    @Override
    public List<SnsAccountMetric> findByAccountIdAndCreatedAtDate(Long accountId, Date date) {
        return snsAccountMetricJpaRepository.findByAccountIdAndCreatedAtDate(accountId, date);
    }

    @Override
    public boolean existsByAccountIdAndCreatedAtAfter(Long accountId, LocalDateTime date) {
        return snsAccountMetricJpaRepository.existsByAccountIdAndCreatedAtAfter(accountId, date);
    }

    @Override
    public void deleteById(Long id) {
        snsAccountMetricJpaRepository.deleteById(id);
    }
}
