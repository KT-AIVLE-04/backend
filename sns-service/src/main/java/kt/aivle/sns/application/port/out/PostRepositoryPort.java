package kt.aivle.sns.application.port.out;

import kt.aivle.sns.domain.model.PostEntity;

import java.util.Optional;

public interface PostRepositoryPort {
    void delete(PostEntity post);

    Optional<PostEntity> findByPostId(String postId);
}
