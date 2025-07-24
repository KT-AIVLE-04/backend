package kt.aivle.auth.application.port.out;

import java.util.Optional;

public interface RefreshTokenRepositoryPort {
    void save(Long userId, String refreshToken, long expirationMs);

    Optional<Long> findUserIdByToken(String refreshToken);

    void delete(String refreshToken);
}
