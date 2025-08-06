package kt.aivle.shorts.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt.aivle.common.exception.InfraException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static kt.aivle.shorts.exception.ShortsErrorCode.KAFKA_ERROR;

@Component
@RequiredArgsConstructor
public class StoreInfoEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(StoreInfoRequestEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("store.info.request", json);
        } catch (Exception e) {
            throw new InfraException(KAFKA_ERROR, e);
        }
    }
}

