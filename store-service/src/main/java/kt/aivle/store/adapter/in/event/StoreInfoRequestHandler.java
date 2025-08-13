package kt.aivle.store.adapter.in.event;

import kt.aivle.store.application.port.in.StoreEventUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreInfoRequestHandler {

    private final StoreEventUseCase storeEventUseCase;

    @KafkaListener(
            topics = "${topic.store.request}",
            groupId = "store-group",
            containerFactory = "requestListenerFactory"
    )
    @SendTo
    public StoreInfoResponseMessage handle(StoreInfoRequestMessage req) {
        return storeEventUseCase.buildResponse(req);
    }
}