package kt.aivle.shorts.adapter.out.event.contents;

import kt.aivle.common.exception.InfraException;
import kt.aivle.shorts.application.port.out.event.contents.ContentServicePort;
import kt.aivle.shorts.application.port.out.event.contents.CreateContentRequest;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static kt.aivle.shorts.exception.ShortsErrorCode.KAFKA_ERROR;

@Component
@RequiredArgsConstructor
public class ContentServiceAdapter implements ContentServicePort {

    private final ReplyingKafkaTemplate<String, CreateContentRequestMessage, Ack> replyingKafkaTemplate;
    private final ContentServiceMapper mapper;

    @Value("${topic.content.request}")
    private String requestTopic;

    @Override
    public Mono<Void> createContent(CreateContentRequest req) {
        CreateContentRequestMessage msg = mapper.toCreateContentRequestMessage(req);

        ProducerRecord<String, CreateContentRequestMessage> record =
                new ProducerRecord<>(requestTopic, String.valueOf(msg.storeId()), msg);

        return Mono.fromFuture(replyingKafkaTemplate.sendAndReceive(record))
                .timeout(Duration.ofSeconds(30))
                .map(ConsumerRecord::value)
                .flatMap(ack -> (ack != null && ack.ok())
                        ? Mono.empty()
                        : Mono.error(new InfraException(KAFKA_ERROR, "contents-service 실패: " + (ack == null ? "null ack" : ack.message()))));
    }
}