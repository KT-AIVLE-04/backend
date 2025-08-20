package kt.aivle.analytics.application.port.out;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;

public interface SnsPostCommentMetricRepositoryPort {
    SnsPostCommentMetric save(SnsPostCommentMetric snsPostCommentMetric);
    Optional<SnsPostCommentMetric> findById(Long id);
    List<SnsPostCommentMetric> findAll();
    List<SnsPostCommentMetric> findByPostId(Long postId);
    List<SnsPostCommentMetric> findByPostIdAndCreatedAtDate(Long postId, Date date);
    List<SnsPostCommentMetric> findByPostIdAndCreatedAtDateWithPagination(Long postId, Date date, Integer page, Integer size);
    Optional<SnsPostCommentMetric> findBySnsCommentId(String snsCommentId);
    void deleteById(Long id);
    void saveAll(List<SnsPostCommentMetric> metrics);
}
