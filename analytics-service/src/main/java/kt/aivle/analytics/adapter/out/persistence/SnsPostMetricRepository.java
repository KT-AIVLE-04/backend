package kt.aivle.analytics.adapter.out.persistence;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.SnsPostMetricJpaRepository;
import kt.aivle.analytics.application.port.out.SnsPostMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsPostMetric;
import lombok.RequiredArgsConstructor;

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
    public List<SnsPostMetric> findByPostIdAndCreatedAtDate(Long postId, Date date) {
        return snsPostMetricJpaRepository.findByPostIdAndCreatedAtDate(postId, date);
    }

    @Override
    public boolean existsByPostIdAndCreatedAtAfter(Long postId, Date date) {
        return snsPostMetricJpaRepository.existsByPostIdAndCreatedAtAfter(postId, date);
    }

    @Override
    public void deleteById(Long id) {
        snsPostMetricJpaRepository.deleteById(id);
    }
}
