package kt.aivle.auth.adapter.out.persistence;

import kt.aivle.auth.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    
    // OAuth 관련 메서드
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    Optional<User> findByEmailAndProviderIsNull(String email);
}
