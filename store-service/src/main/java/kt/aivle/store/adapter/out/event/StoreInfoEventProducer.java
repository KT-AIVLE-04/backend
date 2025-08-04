package kt.aivle.store.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt.aivle.common.exception.InfraException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static kt.aivle.store.exception.StoreErrorCode.KAFKA_ERROR;

@Component
@RequiredArgsConstructor
public class StoreInfoEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(StoreInfoResponseEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("store.info.response", json);
        } catch (Exception e) {
            throw new InfraException(KAFKA_ERROR, e);
        }
    }
}