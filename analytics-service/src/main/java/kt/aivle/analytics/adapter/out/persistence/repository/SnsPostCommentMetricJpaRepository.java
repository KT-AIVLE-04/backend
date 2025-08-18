package kt.aivle.analytics.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;

@Repository
public interface SnsPostCommentMetricJpaRepository extends JpaRepository<SnsPostCommentMetric, Long> {
    List<SnsPostCommentMetric> findByPostId(Long postId);
    List<SnsPostCommentMetric> findByPostIdAndCreatedAtBetween(Long postId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT c FROM SnsPostCommentMetric c WHERE c.postId = :postId AND c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<SnsPostCommentMetric> findByPostIdAndCreatedAtBetweenWithPagination(
        @Param("postId") Long postId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        PageRequest pageRequest);
    
    @Query("SELECT c FROM SnsPostCommentMetric c WHERE c.postId IN :postIds AND c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<SnsPostCommentMetric> findByPostIdsAndCreatedAtBetweenWithPagination(
        @Param("postIds") List<Long> postIds, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        PageRequest pageRequest);
    
    Optional<SnsPostCommentMetric> findBySnsCommentId(String snsCommentId);
}
