package kt.aivle.auth.adapter.out.persistence;

import kt.aivle.auth.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
