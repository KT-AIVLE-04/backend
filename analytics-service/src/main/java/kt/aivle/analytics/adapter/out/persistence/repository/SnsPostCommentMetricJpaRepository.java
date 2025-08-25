package kt.aivle.analytics.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.domain.model.SentimentType;

@Repository
public interface SnsPostCommentMetricJpaRepository extends BaseJpaRepository<SnsPostCommentMetric, Long> {
    List<SnsPostCommentMetric> findByPostId(Long postId);
    
    // 페이지네이션 지원 댓글 조회
    @Query("SELECT c FROM SnsPostCommentMetric c WHERE c.postId = :postId ORDER BY c.createdAt DESC")
    List<SnsPostCommentMetric> findByPostIdWithPagination(@Param("postId") Long postId, PageRequest pageRequest);
    
    // 날짜만 비교하는 메서드
    @Query("SELECT c FROM SnsPostCommentMetric c WHERE c.postId = :postId AND DATE(c.createdAt) = DATE(:date) ORDER BY c.createdAt DESC")
    List<SnsPostCommentMetric> findByPostIdAndCreatedAtDate(@Param("postId") Long postId, @Param("date") LocalDate date);

    // 날짜 기준으로 publishedAt 이전의 댓글을 최신순으로 페이지네이션
    @Query("SELECT c FROM SnsPostCommentMetric c WHERE c.postId = :postId AND DATE(c.publishedAt) <= DATE(:date) ORDER BY c.publishedAt DESC")
    List<SnsPostCommentMetric> findByPostIdAndPublishedAtBeforeWithPagination(@Param("postId") Long postId, @Param("date") LocalDate date, PageRequest pageRequest);

    Optional<SnsPostCommentMetric> findBySnsCommentId(String snsCommentId);
    
    @Modifying
    @Transactional
    @Query("UPDATE SnsPostCommentMetric c SET c.sentiment = :sentiment WHERE c.id = :id")
    void updateSentimentById(@Param("id") Long id, @Param("sentiment") SentimentType sentiment);
}
