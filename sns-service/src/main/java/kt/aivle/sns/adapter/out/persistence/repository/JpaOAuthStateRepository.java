package kt.aivle.sns.adapter.out.persistence.repository;

import kt.aivle.sns.adapter.out.persistence.entity.OAuthStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOAuthStateRepository extends JpaRepository<OAuthStateEntity, String> {
}
