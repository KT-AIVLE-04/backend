package kt.aivle.analytics.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import kt.aivle.analytics.domain.entity.SnsAccount;
import kt.aivle.analytics.domain.model.SnsType;

public interface SnsAccountRepositoryPort {
    SnsAccount save(SnsAccount snsAccount);
    Optional<SnsAccount> findById(Long id);


    long countAll();
    List<SnsAccount> findByUserId(Long userId);
    List<SnsAccount> findByUserIdAndSnsType(Long userId, SnsType snsType);

    void deleteById(Long id);
    List<SnsAccount> findAllWithPagination(int page, int size);
    List<SnsAccount> findAllById(Set<Long> ids);
}
