package kt.aivle.snspost.application.port.out;

import kt.aivle.snspost.domain.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(Long id);

    List<Post> findByUserId(Long userId);

    List<Post> findByStoreId(Long storeId);

    List<Post> findByUserIdAndStoreId(Long userId, Long storeId);

    void deleteById(Long id);

    boolean existsById(Long id);
} 