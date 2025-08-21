package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDate;
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
    public List<SnsPostMetric> findByPostIdAndCreatedAtDate(Long postId, LocalDate date) {
        // LocalDate를 직접 사용 (JpaRepository에서 타임존 처리)
        return snsPostMetricJpaRepository.findByPostIdAndCreatedAtDate(postId, date);
    }

    @Override
    public boolean existsByPostIdAndCreatedAtAfter(Long postId, LocalDate date) {
        // LocalDate를 직접 사용 (JpaRepository에서 타임존 처리)
        return snsPostMetricJpaRepository.existsByPostIdAndCreatedAtAfter(postId, date);
    }

    @Override
    public void deleteById(Long id) {
        snsPostMetricJpaRepository.deleteById(id);
    }
}
