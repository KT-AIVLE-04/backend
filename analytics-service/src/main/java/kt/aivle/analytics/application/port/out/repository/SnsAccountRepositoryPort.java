package kt.aivle.analytics.application.port.out.repository;

import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsAccount;

public interface SnsAccountRepositoryPort {
    SnsAccount save(SnsAccount snsAccount);
    Optional<SnsAccount> findById(Long id);
    List<SnsAccount> findAll();
    long countAll();
    List<SnsAccount> findByUserId(Long userId);
    Optional<SnsAccount> findBySnsAccountId(String snsAccountId);
    void deleteById(Long id);
    List<SnsAccount> findAllWithPagination(int page, int size);
}
