package kt.aivle.analytics.application.port.out;

import java.util.List;
import java.util.Optional;

import kt.aivle.analytics.domain.entity.SnsAccount;

public interface SnsAccountRepositoryPort {
    
    SnsAccount save(SnsAccount snsAccount);
    
    Optional<SnsAccount> findById(Long id);
    
    Optional<SnsAccount> findBySnsAccountId(String snsAccountId);
    
    List<SnsAccount> findByUserId(Long userId);
    
    List<SnsAccount> findByUserIdAndType(Long userId, String type);
    
    List<SnsAccount> findAll();
    
    List<SnsAccount> findAllWithPagination(int page, int size);
    
    void deleteById(Long id);
    
    void deleteBySnsAccountId(String snsAccountId);
}
