package kt.aivle.analytics.adapter.out.persistence.repository;

import kt.aivle.analytics.domain.entity.SnsAccount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnsAccountJpaRepository extends JpaRepository<SnsAccount, Long> {
    List<SnsAccount> findByUserId(Long userId);
    Optional<SnsAccount> findBySnsAccountId(String snsAccountId);
    
    @Query("SELECT sa FROM SnsAccount sa")
    List<SnsAccount> findAllWithPagination(Pageable pageable);
}
