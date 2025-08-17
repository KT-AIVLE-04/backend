package kt.aivle.analytics.application.port.out;

import kt.aivle.analytics.domain.entity.AccountMetric;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountMetricRepositoryPort {
    
    AccountMetric save(AccountMetric accountMetric);
    
    Optional<AccountMetric> findById(Long id);
    
    List<AccountMetric> findBySnsAccountId(Long snsAccountId);
    
    List<AccountMetric> findBySnsAccountIdAndCrawledAtBetween(Long snsAccountId, LocalDateTime startDate, LocalDateTime endDate);
    
    Optional<AccountMetric> findLatestBySnsAccountId(Long snsAccountId);
    
    void deleteBySnsAccountId(Long snsAccountId);
}
