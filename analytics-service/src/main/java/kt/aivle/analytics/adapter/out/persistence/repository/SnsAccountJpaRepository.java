package kt.aivle.analytics.adapter.out.persistence.repository;

import kt.aivle.analytics.domain.entity.SnsAccount;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnsAccountJpaRepository extends JpaRepository<SnsAccount, Long> {
    
    Optional<SnsAccount> findBySnsAccountId(String snsAccountId);
    
    List<SnsAccount> findByUserId(Long userId);
    
    List<SnsAccount> findByUserIdAndType(Long userId, String type);
    
    @Query("SELECT sa FROM SnsAccount sa ORDER BY sa.id")
    List<SnsAccount> findAllWithPagination(Pageable pageable);
    
    default List<SnsAccount> findAllWithPagination(int page, int size) {
        return findAllWithPagination(PageRequest.of(page, size));
    }
    
    void deleteBySnsAccountId(String snsAccountId);
}
