// application/port/out/PostRepositoryPort.java
package kt.aivle.sns.application.port.out;

import kt.aivle.sns.domain.model.PostEntity;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryPort {
    PostEntity save(PostEntity post);

    Optional<PostEntity> findById(Long id);

    void delete(PostEntity post);

    List<PostEntity> findAllByUserIdAndStoreId(Long userId, Long storeId);
}
