package kt.aivle.sns.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kt.aivle.sns.domain.model.PostEntity;

public interface JpaPostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findAllByUserIdAndStoreId(Long userId, Long storeId);
    
    @Query("SELECT p FROM PostEntity p LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<PostEntity> findByIdWithTags(@Param("id") Long id);
}
