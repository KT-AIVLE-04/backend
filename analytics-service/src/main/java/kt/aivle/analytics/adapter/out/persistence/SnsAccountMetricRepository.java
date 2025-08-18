package kt.aivle.analytics.adapter.out.persistence;

import kt.aivle.analytics.application.port.out.SnsAccountMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsAccountMetric;
import kt.aivle.analytics.adapter.out.persistence.repository.SnsAccountMetricJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public List<SnsAccountMetric> findByAccountIdAndCrawledAtBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return snsAccountMetricJpaRepository.findByAccountIdAndCrawledAtBetween(accountId, startDate, endDate);
    }

    @Override
    public boolean existsByAccountIdAndCrawledAtAfter(Long accountId, LocalDateTime date) {
        return snsAccountMetricJpaRepository.existsByAccountIdAndCrawledAtAfter(accountId, date);
    }

    @Override
    public void deleteById(Long id) {
        snsAccountMetricJpaRepository.deleteById(id);
    }
}
