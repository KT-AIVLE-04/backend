package kt.aivle.analytics.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.SnsPostMetric;

@Repository
public interface SnsPostMetricJpaRepository extends BaseJpaRepository<SnsPostMetric, Long> {
    boolean existsByPostIdAndCreatedAtAfter(Long postId, LocalDateTime date);
    
    // 날짜만 비교하는 메서드
    @Query("SELECT m FROM SnsPostMetric m WHERE m.postId = :postId AND DATE(m.createdAt) = DATE(:date)")
    List<SnsPostMetric> findByPostIdAndCreatedAtDate(@Param("postId") Long postId, @Param("date") LocalDate date);
    
    /**
     * Post Metrics를 Post와 Account 정보와 함께 조회 (JOIN 쿼리)
     */
    @Query("""
        SELECT m, p, a FROM SnsPostMetric m
        JOIN SnsPost p ON m.postId = p.id
        JOIN SnsAccount a ON p.accountId = a.id
        WHERE m.postId IN :postIds AND DATE(m.createdAt) = DATE(:date)
        """)
    List<Object[]> findMetricsWithPostAndAccount(@Param("postIds") List<Long> postIds, @Param("date") LocalDate date);
}
