package kt.aivle.analytics.application.port.out.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.domain.model.SentimentType;

public interface SnsPostCommentMetricRepositoryPort {
    SnsPostCommentMetric save(SnsPostCommentMetric snsPostCommentMetric);
    Optional<SnsPostCommentMetric> findById(Long id);
    List<SnsPostCommentMetric> findByPostId(Long postId);
    List<SnsPostCommentMetric> findByPostIdWithPagination(Long postId, int page, int size);
    List<SnsPostCommentMetric> findByPostIdAndCreatedAtDate(Long postId, LocalDate date);
    
    // 날짜 기준으로 publishedAt 이전의 댓글을 최신순으로 페이지네이션
    List<SnsPostCommentMetric> findByPostIdAndPublishedAtBeforeWithPagination(Long postId, LocalDate date, int page, int size);
    
    Optional<SnsPostCommentMetric> findBySnsCommentId(String snsCommentId);
    void deleteById(Long id);
    void saveAll(List<SnsPostCommentMetric> metrics);
    void updateSentimentById(Long id, SentimentType sentiment);
}
