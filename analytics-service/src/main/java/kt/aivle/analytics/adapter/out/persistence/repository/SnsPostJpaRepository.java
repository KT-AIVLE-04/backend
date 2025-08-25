package kt.aivle.analytics.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kt.aivle.analytics.domain.entity.SnsPost;

@Repository
public interface SnsPostJpaRepository extends BaseJpaRepository<SnsPost, Long> {
    List<SnsPost> findByAccountId(Long accountId);
    Optional<SnsPost> findBySnsPostId(String snsPostId);
    
    @Query("SELECT p FROM SnsPost p WHERE p.accountId = :accountId ORDER BY p.createdAt DESC LIMIT 1")
    Optional<SnsPost> findLatestByAccountId(@Param("accountId") Long accountId);
}
