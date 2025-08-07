package kt.aivle.shorts.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt.aivle.common.exception.InfraException;
import kt.aivle.shorts.application.port.out.ContentsEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static kt.aivle.shorts.exception.ShortsErrorCode.KAFKA_ERROR;

@Component
@RequiredArgsConstructor
public class ContentEventProducer implements ContentsEventPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publish(CreateContentRequestEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("content.request", json);
        } catch (Exception e) {
            throw new InfraException(KAFKA_ERROR, e);
        }
        return Mono.empty();
    }
}

