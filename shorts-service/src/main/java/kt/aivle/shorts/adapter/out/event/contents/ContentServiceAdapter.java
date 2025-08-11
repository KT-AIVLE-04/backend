package kt.aivle.shorts.adapter.out.event.contents;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt.aivle.common.exception.InfraException;
import kt.aivle.shorts.application.port.out.event.contents.ContentServicePort;
import kt.aivle.shorts.application.port.out.event.contents.CreateContentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static kt.aivle.shorts.exception.ShortsErrorCode.KAFKA_ERROR;

@Component
@RequiredArgsConstructor
public class ContentServiceAdapter implements ContentServicePort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ContentServiceMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> createContent(CreateContentRequest request) {
        CreateContentRequestMessage msg = mapper.toMessage(request);
        try {
            String json = objectMapper.writeValueAsString(msg);
            kafkaTemplate.send("content.request", json);
        } catch (Exception e) {
            throw new InfraException(KAFKA_ERROR, e);
        }
        return Mono.empty();
    }
}

