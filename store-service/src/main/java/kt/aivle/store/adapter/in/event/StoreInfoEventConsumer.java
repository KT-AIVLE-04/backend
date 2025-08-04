package kt.aivle.store.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt.aivle.common.exception.InfraException;
import kt.aivle.store.application.port.in.StoreEventUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static kt.aivle.store.exception.StoreErrorCode.KAFKA_ERROR;

@Component
@RequiredArgsConstructor
public class StoreInfoEventConsumer {
    private final StoreEventUseCase useCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "store.info.request", groupId = "store-group")
    public void listen(String message) {
        try {
            StoreInfoRequestEvent event = objectMapper.readValue(message, StoreInfoRequestEvent.class);
            useCase.handleStoreInfoRequest(event);
        } catch (Exception e) {
            throw new InfraException(KAFKA_ERROR, e);
        }
    }
}