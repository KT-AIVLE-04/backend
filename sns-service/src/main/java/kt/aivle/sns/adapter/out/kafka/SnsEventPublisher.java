package kt.aivle.sns.adapter.out.kafka;

import kt.aivle.sns.application.event.PostEvent;
import kt.aivle.sns.application.event.SnsAccountEvent;
import kt.aivle.sns.application.messaging.Topics;
import kt.aivle.sns.application.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;


@Slf4j
@Component
@RequiredArgsConstructor
public class SnsEventPublisher implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishPostCreated(PostEvent e) {
        kafkaTemplate.send(Topics.POST_CREATED, e.getAccountId().toString(), e);
    }

    @Override
    public void publishPostDeleted(PostEvent e) {
        kafkaTemplate.send(Topics.POST_DELETED, e.getAccountId().toString(), e);
    }

    @Override
    public void publishSnsAccountConnected(SnsAccountEvent e) {
        kafkaTemplate.send(Topics.SNS_ACCOUNT_CONNECTED, e.getSnsAccountId(), e);
    }

    @Override
    public void publishSnsAccountDisconnected(SnsAccountEvent e) {
        kafkaTemplate.send(Topics.SNS_ACCOUNT_DISCONNECTED, e.getSnsAccountId(), e);
    }
}
