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
    List<SnsPostMetric> findByPostId(Long postId);
    boolean existsByPostIdAndCreatedAtAfter(Long postId, LocalDateTime date);
    
    // 날짜만 비교하는 메서드 (타임존 명시)
    @Query("SELECT m FROM SnsPostMetric m WHERE m.postId = :postId AND DATE(CONVERT_TZ(m.createdAt, '+00:00', '+09:00')) = DATE(:date)")
    List<SnsPostMetric> findByPostIdAndCreatedAtDate(@Param("postId") Long postId, @Param("date") LocalDate date);
}
