package kt.aivle.analytics.adapter.out.persistence;

import kt.aivle.analytics.application.port.out.SnsPostMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsPostMetric;
import kt.aivle.analytics.adapter.out.persistence.repository.SnsPostMetricJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SnsPostMetricRepository implements SnsPostMetricRepositoryPort {

    private final SnsPostMetricJpaRepository snsPostMetricJpaRepository;

    @Override
    public SnsPostMetric save(SnsPostMetric snsPostMetric) {
        return snsPostMetricJpaRepository.save(snsPostMetric);
    }

    @Override
    public Optional<SnsPostMetric> findById(Long id) {
        return snsPostMetricJpaRepository.findById(id);
    }

    @Override
    public List<SnsPostMetric> findAll() {
        return snsPostMetricJpaRepository.findAll();
    }

    @Override
    public List<SnsPostMetric> findByPostId(Long postId) {
        return snsPostMetricJpaRepository.findByPostId(postId);
    }

    @Override
    public List<SnsPostMetric> findByPostIdAndCrawledAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate) {
        return snsPostMetricJpaRepository.findByPostIdAndCrawledAtBetween(postId, startDate, endDate);
    }

    @Override
    public boolean existsByPostIdAndCrawledAtAfter(Long postId, LocalDateTime date) {
        return snsPostMetricJpaRepository.existsByPostIdAndCrawledAtAfter(postId, date);
    }

    @Override
    public void deleteById(Long id) {
        snsPostMetricJpaRepository.deleteById(id);
    }
}
