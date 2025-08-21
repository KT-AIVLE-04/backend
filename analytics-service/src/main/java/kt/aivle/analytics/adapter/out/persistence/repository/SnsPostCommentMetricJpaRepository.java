package kt.aivle.analytics.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;

@Repository
public interface SnsPostCommentMetricJpaRepository extends BaseJpaRepository<SnsPostCommentMetric, Long> {
    List<SnsPostCommentMetric> findByPostId(Long postId);
    
    // 날짜만 비교하는 메서드 (타임존 명시)
    @Query("SELECT c FROM SnsPostCommentMetric c WHERE c.postId = :postId AND DATE(CONVERT_TZ(c.createdAt, '+00:00', '+09:00')) = DATE(:date) ORDER BY c.createdAt DESC")
    List<SnsPostCommentMetric> findByPostIdAndCreatedAtDate(@Param("postId") Long postId, @Param("date") LocalDate date);
    
    @Query("SELECT c FROM SnsPostCommentMetric c WHERE c.postId = :postId AND DATE(CONVERT_TZ(c.createdAt, '+00:00', '+09:00')) = DATE(:date) ORDER BY c.createdAt DESC")
    List<SnsPostCommentMetric> findByPostIdAndCreatedAtDateWithPagination(
        @Param("postId") Long postId, 
        @Param("date") LocalDate date, 
        PageRequest pageRequest);
    
    Optional<SnsPostCommentMetric> findBySnsCommentId(String snsCommentId);
}
