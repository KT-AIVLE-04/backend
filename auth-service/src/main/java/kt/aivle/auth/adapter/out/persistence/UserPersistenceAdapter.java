package kt.aivle.auth.adapter.out.persistence;

import kt.aivle.auth.application.port.out.UserRepositoryPort;
import kt.aivle.auth.domain.model.User;
import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static kt.aivle.auth.exception.AuthErrorCode.NOT_FOUND_EMAIL;

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
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new BusinessException(NOT_FOUND_EMAIL));
    }
}
