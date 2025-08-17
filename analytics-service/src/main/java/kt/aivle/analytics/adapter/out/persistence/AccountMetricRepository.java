package kt.aivle.analytics.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.AccountMetricJpaRepository;
import kt.aivle.analytics.application.port.out.AccountMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.AccountMetric;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AccountMetricRepository implements AccountMetricRepositoryPort {
    
    private final AccountMetricJpaRepository accountMetricJpaRepository;
    
    @Override
    public AccountMetric save(AccountMetric accountMetric) {
        return accountMetricJpaRepository.save(accountMetric);
    }
    
    @Override
    public Optional<AccountMetric> findById(Long id) {
        return accountMetricJpaRepository.findById(id);
    }
    
    @Override
    public List<AccountMetric> findBySnsAccountId(Long snsAccountId) {
        return accountMetricJpaRepository.findBySnsAccountId(snsAccountId);
    }
    
    @Override
    public List<AccountMetric> findBySnsAccountIdAndCrawledAtBetween(Long snsAccountId, LocalDateTime startDate, LocalDateTime endDate) {
        return accountMetricJpaRepository.findBySnsAccountIdAndCrawledAtBetween(snsAccountId, startDate, endDate);
    }
    
    @Override
    public Optional<AccountMetric> findLatestBySnsAccountId(Long snsAccountId) {
        return accountMetricJpaRepository.findLatestBySnsAccountId(snsAccountId);
    }
    
    @Override
    public void deleteBySnsAccountId(Long snsAccountId) {
        accountMetricJpaRepository.deleteBySnsAccountId(snsAccountId);
    }
}
