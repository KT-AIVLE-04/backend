package kt.aivle.auth.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import kt.aivle.auth.application.port.out.UserRepositoryPort;
import kt.aivle.auth.domain.model.OAuthProvider;
import kt.aivle.auth.domain.model.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final JpaUserRepository userRepository;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider.name(), providerId);
    }
}
