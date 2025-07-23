package kt.aivle.auth.application.port.out;

import kt.aivle.auth.domain.model.User;

public interface UserRepositoryPort {
    boolean existsByEmail(String email);

    User save(User user);
}
