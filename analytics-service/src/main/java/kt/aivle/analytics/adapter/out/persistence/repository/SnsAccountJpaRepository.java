package kt.aivle.analytics.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.SnsAccount;

@Repository
public interface SnsAccountJpaRepository extends BaseJpaRepository<SnsAccount, Long> {
    List<SnsAccount> findByUserId(Long userId);
    Optional<SnsAccount> findBySnsAccountId(String snsAccountId);
}
