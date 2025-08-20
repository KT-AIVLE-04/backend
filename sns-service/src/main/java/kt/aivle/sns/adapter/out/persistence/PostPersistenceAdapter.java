package kt.aivle.sns.adapter.out.persistence;

import kt.aivle.sns.application.port.out.PostRepositoryPort;
import kt.aivle.sns.domain.model.PostEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepositoryPort {

    private final JpaPostRepository postRepository;

    @Override
    public void delete(PostEntity post) {
        postRepository.delete(post);
    }

    @Override
    public Optional<PostEntity> findByPostId(String postId) {
        return postRepository.findByPostId(postId);
    }
}
