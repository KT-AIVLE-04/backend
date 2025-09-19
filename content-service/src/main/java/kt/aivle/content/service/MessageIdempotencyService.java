package kt.aivle.content.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MessageIdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PROCESSED_MESSAGE_PREFIX = "processed_message:";
    private static final Duration TTL = Duration.ofDays(7);

    public boolean isAlreadyProcessed(String requestId) {
        String key = PROCESSED_MESSAGE_PREFIX + requestId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void markAsProcessed(String requestId) {
        String key = PROCESSED_MESSAGE_PREFIX + requestId;
        redisTemplate.opsForValue().set(key, "processed", TTL);
    }
}