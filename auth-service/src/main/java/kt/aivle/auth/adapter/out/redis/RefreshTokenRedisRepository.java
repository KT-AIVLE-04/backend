package kt.aivle.auth.adapter.out.redis;

import kt.aivle.auth.application.port.out.RefreshTokenRepositoryPort;
import kt.aivle.common.exception.InfraException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static kt.aivle.common.code.CommonResponseCode.INTERNAL_SERVER_ERROR;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository implements RefreshTokenRepositoryPort {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "refresh_token:";

    @Override
    public void save(Long userId, String refreshToken, long expiresInMillis) {
        String key = PREFIX + refreshToken;
        try {
            redisTemplate.opsForValue().set(key, String.valueOf(userId), expiresInMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new InfraException(INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public Optional<Long> findUserIdByToken(String refreshToken) {
        String key = PREFIX + refreshToken;
        String value = redisTemplate.opsForValue().get(key);
        return (value != null) ? Optional.of(Long.parseLong(value)) : Optional.empty();
    }

    @Override
    public void delete(String refreshToken) {
        String key = PREFIX + refreshToken;
        redisTemplate.delete(key);
    }
}
