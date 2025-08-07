package kt.aivle.sns.application.port.out;

import kt.aivle.sns.domain.model.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Optional<PostEntity> findByPostId(String postId);

    List<PostEntity> findAllByUserId(String userId);
}
