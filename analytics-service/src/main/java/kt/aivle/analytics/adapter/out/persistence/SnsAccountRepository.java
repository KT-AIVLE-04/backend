package kt.aivle.analytics.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kt.aivle.analytics.adapter.out.persistence.repository.SnsAccountJpaRepository;
import kt.aivle.analytics.application.port.out.SnsAccountRepositoryPort;
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
    public Optional<SnsAccount> findBySnsAccountId(String snsAccountId) {
        return snsAccountJpaRepository.findBySnsAccountId(snsAccountId);
    }
    
    @Override
    public List<SnsAccount> findByUserId(Long userId) {
        return snsAccountJpaRepository.findByUserId(userId);
    }
    
    @Override
    public List<SnsAccount> findByUserIdAndType(Long userId, String type) {
        return snsAccountJpaRepository.findByUserIdAndType(userId, type);
    }
    
    @Override
    public List<SnsAccount> findAll() {
        return snsAccountJpaRepository.findAll();
    }
    
    @Override
    public List<SnsAccount> findAllWithPagination(int page, int size) {
        return snsAccountJpaRepository.findAllWithPagination(page, size);
    }
    
    @Override
    public void deleteById(Long id) {
        snsAccountJpaRepository.deleteById(id);
    }
    
    @Override
    public void deleteBySnsAccountId(String snsAccountId) {
        snsAccountJpaRepository.deleteBySnsAccountId(snsAccountId);
    }
}
