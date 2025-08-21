package kt.aivle.analytics.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.SnsAccountMetric;

@Repository
public interface SnsAccountMetricJpaRepository extends BaseJpaRepository<SnsAccountMetric, Long> {
    List<SnsAccountMetric> findByAccountId(Long accountId);
    boolean existsByAccountIdAndCreatedAtAfter(Long accountId, LocalDate date);
    
    // 날짜만 비교하는 메서드 (타임존 명시)
    @Query("SELECT m FROM SnsAccountMetric m WHERE m.accountId = :accountId AND DATE(CONVERT_TZ(m.createdAt, '+00:00', '+09:00')) = DATE(:date)")
    List<SnsAccountMetric> findByAccountIdAndCreatedAtDate(@Param("accountId") Long accountId, @Param("date") LocalDate date);
}
