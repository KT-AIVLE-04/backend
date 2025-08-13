package kt.aivle.shorts.adapter.out.event.contents;

import kt.aivle.common.exception.InfraException;
import kt.aivle.shorts.application.port.out.event.contents.ContentServicePort;
import kt.aivle.shorts.application.port.out.event.contents.CreateContentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static kt.aivle.shorts.exception.ShortsErrorCode.KAFKA_ERROR;

@Component
@RequiredArgsConstructor
public class ContentServiceAdapter implements ContentServicePort {

    private final KafkaTemplate<String, Object> eventKafkaTemplate;
    private final ContentServiceMapper mapper;

    @Value("${topic.content.request}")
    private String contentTopic;

    @Override
    public Mono<Void> createContent(CreateContentRequest request) {
        CreateContentRequestMessage msg = mapper.toCreateContentRequestMessage(request);
        try {
            String key = String.valueOf(msg.storeId());
            return Mono.fromFuture(eventKafkaTemplate.send(contentTopic, key, msg).toCompletableFuture()).then();
        } catch (Exception e) {
            throw new InfraException(KAFKA_ERROR, e);
        }
    }
}