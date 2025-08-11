package kt.aivle.auth.application.port.out;

import java.util.Optional;

import kt.aivle.auth.domain.model.OAuthProvider;
import kt.aivle.auth.domain.model.User;

public interface UserRepositoryPort {
    boolean existsByEmail(String email);

    User save(User user);

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}
