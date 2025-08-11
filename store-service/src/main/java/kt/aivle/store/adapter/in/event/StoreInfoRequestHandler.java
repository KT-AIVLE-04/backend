package kt.aivle.store.adapter.in.event;

import kt.aivle.store.application.port.in.StoreEventUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
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
    @SendTo("${topic.store.reply}")
    public Message<StoreInfoResponseMessage> handle(
            StoreInfoRequestMessage req,
            @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId
    ) {
        StoreInfoResponseMessage resp = storeEventUseCase.buildResponse(req);

        return MessageBuilder
                .withPayload(resp)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();
    }
}