package kt.aivle.analytics.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import kt.aivle.analytics.domain.entity.SnsAccount;


public interface SnsAccountRepositoryPort {
    SnsAccount save(SnsAccount snsAccount);
    Optional<SnsAccount> findById(Long id);


    long countAll();
    List<SnsAccount> findByUserId(Long userId);
    
    // accountId로 userId 조회 (id 필드가 accountId 역할)
    Optional<Long> findUserIdById(Long accountId);

    void deleteById(Long id);
    List<SnsAccount> findAllWithPagination(int page, int size);
    List<SnsAccount> findAllById(Set<Long> ids);
}
