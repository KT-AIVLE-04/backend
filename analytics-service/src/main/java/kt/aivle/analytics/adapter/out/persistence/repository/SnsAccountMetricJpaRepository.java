package kt.aivle.analytics.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.SnsAccountMetric;

@Repository
public interface SnsAccountMetricJpaRepository extends BaseJpaRepository<SnsAccountMetric, Long> {
    boolean existsByAccountIdAndCreatedAtAfter(Long accountId, LocalDateTime date);
    
    // 날짜만 비교하는 메서드
    @Query("SELECT m FROM SnsAccountMetric m WHERE m.accountId = :accountId AND DATE(m.createdAt) = DATE(:date)")
    List<SnsAccountMetric> findByAccountIdAndCreatedAtDate(@Param("accountId") Long accountId, @Param("date") LocalDate date);
    
    /**
     * Account Metrics를 Account 정보와 함께 조회 (JOIN 쿼리)
     */
    @Query("""
        SELECT m, a FROM SnsAccountMetric m
        JOIN SnsAccount a ON m.accountId = a.id
        WHERE m.accountId IN :accountIds AND DATE(m.createdAt) = DATE(:date)
        """)
    List<Object[]> findMetricsWithAccount(@Param("accountIds") List<Long> accountIds, @Param("date") LocalDate date);
}
