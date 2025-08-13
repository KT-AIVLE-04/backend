package kt.aivle.snspost.adapter.out.persistence;

import kt.aivle.snspost.application.port.out.PostRepository;
import kt.aivle.snspost.domain.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postJpaRepository.findById(id);
    }

    @Override
    public List<Post> findByUserId(Long userId) {
        return postJpaRepository.findByUserId(userId);
    }

    @Override
    public List<Post> findByStoreId(Long storeId) {
        return postJpaRepository.findByStoreId(storeId);
    }

    @Override
    public List<Post> findByUserIdAndStoreId(Long userId, Long storeId) {
        return postJpaRepository.findByUserIdAndStoreId(userId, storeId);
    }

    @Override
    public void deleteById(Long id) {
        postJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return postJpaRepository.existsById(id);
    }
} 