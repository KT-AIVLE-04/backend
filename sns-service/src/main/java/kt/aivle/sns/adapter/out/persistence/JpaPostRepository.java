package kt.aivle.sns.adapter.out.persistence;

import kt.aivle.sns.domain.model.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPostRepository extends JpaRepository<PostEntity, Long> {
    Optional<PostEntity> findByPostId(String postId);
}
