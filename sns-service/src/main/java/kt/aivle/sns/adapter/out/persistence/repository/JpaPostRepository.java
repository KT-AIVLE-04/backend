package kt.aivle.sns.adapter.out.persistence.repository;

import kt.aivle.sns.domain.model.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaPostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findAllByUserIdAndStoreId(Long userId, Long storeId);
}
