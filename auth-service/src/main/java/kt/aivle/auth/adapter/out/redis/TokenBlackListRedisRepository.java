package kt.aivle.auth.adapter.out.redis;

import kt.aivle.auth.application.port.out.TokenBlacklistRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class TokenBlackListRedisRepository implements TokenBlacklistRepositoryPort {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "blacklist:access_token:";

    @Override
    public void addAccessTokenToBlacklist(String jti, long expirationMillis) {
        String key = PREFIX + jti;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        String key = PREFIX + jti;
        return redisTemplate.hasKey(key);
    }
}
