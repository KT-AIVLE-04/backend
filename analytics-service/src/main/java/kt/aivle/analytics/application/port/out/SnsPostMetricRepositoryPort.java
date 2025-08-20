package kt.aivle.analytics.application.port.out;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsPostMetric;

public interface SnsPostMetricRepositoryPort {
    SnsPostMetric save(SnsPostMetric snsPostMetric);
    Optional<SnsPostMetric> findById(Long id);
    List<SnsPostMetric> findAll();
    List<SnsPostMetric> findByPostId(Long postId);
    List<SnsPostMetric> findByPostIdAndCreatedAtDate(Long postId, Date date);
    boolean existsByPostIdAndCreatedAtAfter(Long postId, Date date);
    void deleteById(Long id);
}
