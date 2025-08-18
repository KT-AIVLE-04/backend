package kt.aivle.analytics.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;

public interface SnsPostCommentMetricRepositoryPort {
    SnsPostCommentMetric save(SnsPostCommentMetric snsPostCommentMetric);
    Optional<SnsPostCommentMetric> findById(Long id);
    List<SnsPostCommentMetric> findAll();
    List<SnsPostCommentMetric> findByPostId(Long postId);
    List<SnsPostCommentMetric> findByPostIdAndCrawledAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate);
    List<SnsPostCommentMetric> findByPostIdAndCrawledAtBetweenWithPagination(Long postId, LocalDateTime startDate, LocalDateTime endDate, Integer page, Integer size);
    List<SnsPostCommentMetric> findByPostIdsAndCrawledAtBetweenWithPagination(List<Long> postIds, LocalDateTime startDate, LocalDateTime endDate, Integer page, Integer size);
    Optional<SnsPostCommentMetric> findBySnsCommentId(String snsCommentId);
    void deleteById(Long id);
}
