package kt.aivle.analytics.adapter.out.persistence.repository;

import kt.aivle.analytics.domain.entity.SnsPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnsPostJpaRepository extends JpaRepository<SnsPost, Long> {
    List<SnsPost> findByAccountId(Long accountId);
    Optional<SnsPost> findBySnsPostId(String snsPostId);
    
    @Query("SELECT sp FROM SnsPost sp")
    List<SnsPost> findAllWithPagination(Pageable pageable);
}
