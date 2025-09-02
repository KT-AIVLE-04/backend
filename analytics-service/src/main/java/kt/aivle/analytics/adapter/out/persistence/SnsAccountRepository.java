package kt.aivle.analytics.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.SnsAccountJpaRepository;
import kt.aivle.analytics.application.port.out.repository.SnsAccountRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsAccount;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SnsAccountRepository implements SnsAccountRepositoryPort {

    private final SnsAccountJpaRepository snsAccountJpaRepository;

    @Override
    public SnsAccount save(SnsAccount snsAccount) {
        return snsAccountJpaRepository.save(snsAccount);
    }

    @Override
    public Optional<SnsAccount> findById(Long id) {
        return snsAccountJpaRepository.findById(id);
    }

    @Override
    public long countAll() {
        return snsAccountJpaRepository.count();
    }

    @Override
    public List<SnsAccount> findByUserId(Long userId) {
        return snsAccountJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<Long> findUserIdByAccountId(Long accountId) {
        return snsAccountJpaRepository.findUserIdByAccountId(accountId);
    }

    @Override
    public void deleteById(Long id) {
        snsAccountJpaRepository.deleteById(id);
    }

    @Override
    public List<SnsAccount> findAllWithPagination(int page, int size) {
        return snsAccountJpaRepository.findAllWithPagination(PageRequest.of(page, size));
    }
    
    @Override
    public List<SnsAccount> findAllById(Set<Long> ids) {
        return snsAccountJpaRepository.findAllById(ids);
    }
}
