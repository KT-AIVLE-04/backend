package kt.aivle.analytics.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.AccountMetric;

@Repository
public interface AccountMetricJpaRepository extends JpaRepository<AccountMetric, Long> {
    
    List<AccountMetric> findBySnsAccountId(Long snsAccountId);
    
    List<AccountMetric> findBySnsAccountIdAndCrawledAtBetween(Long snsAccountId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT am FROM AccountMetric am WHERE am.snsAccountId = :snsAccountId ORDER BY am.crawledAt DESC LIMIT 1")
    Optional<AccountMetric> findLatestBySnsAccountId(@Param("snsAccountId") Long snsAccountId);
    
    void deleteBySnsAccountId(Long snsAccountId);
}
