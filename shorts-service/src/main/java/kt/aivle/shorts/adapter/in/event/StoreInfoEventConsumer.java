package kt.aivle.shorts.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt.aivle.common.exception.InfraException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static kt.aivle.shorts.exception.ShortsErrorCode.KAFKA_ERROR;

@Component
@RequiredArgsConstructor
public class StoreInfoEventConsumer {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "store.info.response", groupId = "shorts-group")
    public void listen(String message) {
        try {
            StoreInfoResponseEvent event = objectMapper.readValue(message, StoreInfoResponseEvent.class);
            String redisKey = "STORE_INFO:" + event.requestId();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                String value = objectMapper.writeValueAsString(event);
                redisTemplate.opsForValue().set(redisKey, value, Duration.ofSeconds(30));
            }
        } catch (Exception e) {
            throw new InfraException(KAFKA_ERROR, e);
        }
    }
}