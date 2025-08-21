// adapter/out/persistence/PostPersistenceAdapter.java
package kt.aivle.sns.adapter.out.persistence;

import kt.aivle.sns.adapter.out.persistence.repository.JpaPostRepository;
import kt.aivle.sns.application.port.out.PostRepositoryPort;
import kt.aivle.sns.domain.model.PostEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepositoryPort {

    private final JpaPostRepository postRepository;

    @Override
    public PostEntity save(PostEntity post) {
        return postRepository.save(post);
    }

    @Override
    public void delete(PostEntity post) {
        postRepository.delete(post);
    }

    @Override
    public List<PostEntity> findAllByUserIdAndStoreId(Long userId, Long storeId) {
        return postRepository.findAllByUserIdAndStoreId(userId, storeId);
    }

    @Override
    public Optional<PostEntity> findById(Long id) {
        return postRepository.findById(id);
    }
}
