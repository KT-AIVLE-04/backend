package kt.aivle.auth.application.port.out;

import kt.aivle.auth.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    boolean existsByEmail(String email);

    User save(User user);

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);


}
