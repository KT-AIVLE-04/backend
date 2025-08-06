package kt.aivle.shorts.adapter.out.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt.aivle.shorts.adapter.in.event.StoreInfoResponseEvent;
import kt.aivle.shorts.adapter.out.event.StoreInfoEventProducer;
import kt.aivle.shorts.adapter.out.event.StoreInfoRequestEvent;
import kt.aivle.shorts.application.port.out.StoreInfoQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StoreInfoRedisAdapter implements StoreInfoQueryPort {

    private final StringRedisTemplate redisTemplate;
    private final StoreInfoEventProducer producer;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<StoreInfoResponseEvent> getStoreInfo(Long storeId, Long userId) {
        String requestId = UUID.randomUUID().toString();
        StoreInfoRequestEvent requestEvent = new StoreInfoRequestEvent(requestId, storeId, userId);

        String redisKey = "STORE_INFO:" + requestId;
        redisTemplate.opsForValue().set(redisKey, "WAITING", Duration.ofSeconds(10));

        producer.send(requestEvent);

        return Flux.interval(Duration.ZERO, Duration.ofMillis(100))
                .flatMap(tick ->
                        Mono.fromCallable(() -> redisTemplate.opsForValue().get(redisKey))
                )
                .filter(val -> val != null && !"WAITING".equals(val))
                .next()
                .flatMap(json ->
                        Mono.fromCallable(() ->
                                objectMapper.readValue(json, StoreInfoResponseEvent.class)
                        )
                )
                .timeout(Duration.ofSeconds(10));
    }
}
