package kt.aivle.shorts.adapter.out.event.store;

import kt.aivle.shorts.adapter.out.event.store.dto.StoreInfoRequestMessage;
import kt.aivle.shorts.adapter.out.event.store.dto.StoreInfoResponseMessage;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoRequest;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoResponse;
import kt.aivle.shorts.application.port.out.event.store.StoreServicePort;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class StoreServiceAdapter implements StoreServicePort {

    private final ReplyingKafkaTemplate<String, StoreInfoRequestMessage, StoreInfoResponseMessage> replyingKafkaTemplate;
    private final StoreServiceMapper mapper;

    @Value("${topic.store.request}")
    private String requestTopic;

    @Override
    public Mono<StoreInfoResponse> getStoreInfo(StoreInfoRequest req) {
        StoreInfoRequestMessage msg = mapper.toStoreInfoRequestMessage(req);
        ProducerRecord<String, StoreInfoRequestMessage> record =
                new ProducerRecord<>(requestTopic, String.valueOf(msg.storeId()), msg);

        CompletableFuture<ConsumerRecord<String, StoreInfoResponseMessage>> future =
                replyingKafkaTemplate.sendAndReceive(record);

        return Mono.fromFuture(future)
                .timeout(Duration.ofSeconds(10))
                .map(ConsumerRecord::value)
                .map(mapper::toStoreInfoResponse);
    }
}