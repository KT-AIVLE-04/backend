package kt.aivle.snspost.adapter.out.persistence;

import kt.aivle.snspost.domain.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostJpaRepository extends JpaRepository<Post, Long> {

    List<Post> findByUserId(Long userId);

    List<Post> findByStoreId(Long storeId);

    List<Post> findByUserIdAndStoreId(Long userId, Long storeId);

    List<Post> findByUserIdAndIsPublicTrue(Long userId);

    List<Post> findByStoreIdAndIsPublicTrue(Long storeId);
} 