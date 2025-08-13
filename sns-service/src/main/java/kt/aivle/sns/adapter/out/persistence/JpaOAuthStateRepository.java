package kt.aivle.sns.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOAuthStateRepository extends JpaRepository<OAuthStateEntity, String> {
}
