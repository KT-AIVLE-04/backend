package kt.aivle.analytics.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.SnsAccount;
import kt.aivle.analytics.domain.model.SnsType;

@Repository
public interface SnsAccountJpaRepository extends BaseJpaRepository<SnsAccount, Long> {
    List<SnsAccount> findByUserId(Long userId);
    List<SnsAccount> findByUserIdAndType(Long userId, SnsType type);
    
    // accountId로 userId 조회 (id 필드가 accountId 역할)
    Optional<Long> findUserIdById(Long accountId);
}
