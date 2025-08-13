package kt.aivle.analytics.adapter.out.event;

import static kt.aivle.analytics.exception.AnalyticsErrorCode.KAFKA_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.out.event.dto.SnsTokenRequestEvent;
import kt.aivle.common.exception.InfraException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SnsTokenEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(SnsTokenResponseEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("sns-token.response", json);
        } catch (Exception e) {
            throw new InfraException(KAFKA_ERROR, e);
        }
    }
    
    public void sendRequest(SnsTokenRequestEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("sns-token.request", json);
        } catch (Exception e) {
            throw new InfraException(KAFKA_ERROR, e);
        }
    }
}
