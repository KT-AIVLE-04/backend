package kt.aivle.analytics.application.port.out;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsPostMetric;

public interface SnsPostMetricRepositoryPort {
    SnsPostMetric save(SnsPostMetric snsPostMetric);
    Optional<SnsPostMetric> findById(Long id);
    List<SnsPostMetric> findAll();
    List<SnsPostMetric> findByPostId(Long postId);
    List<SnsPostMetric> findByPostIdAndCreatedAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate);
    List<SnsPostMetric> findByPostIdAndCreatedAtDate(Long postId, Date date);
    boolean existsByPostIdAndCreatedAtAfter(Long postId, LocalDateTime date);
    void deleteById(Long id);
}
