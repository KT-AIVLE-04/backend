package kt.aivle.analytics.adapter.out.event;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.in.event.dto.PostInfoRequestMessage;
import kt.aivle.analytics.adapter.in.event.dto.PostInfoResponseMessage;
import kt.aivle.analytics.application.port.out.SnsServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnsServiceAdapter implements SnsServicePort {

    private final ReplyingKafkaTemplate<String, PostInfoRequestMessage, PostInfoResponseMessage> replyingKafkaTemplate;

    private final String requestTopic = "post-info.request";

    @Override
    public Mono<PostInfoResponseMessage> getPostInfo(Long postId, Long userId, Long accountId, Long storeId) {
        log.info("📤 [KAFKA] Sending request - postId: {}", postId);
        
        PostInfoRequestMessage request = PostInfoRequestMessage.builder()
                .postId(postId)
                .userId(userId)
                .accountId(accountId)
                .storeId(storeId)
                .build();

        ProducerRecord<String, PostInfoRequestMessage> record =
                new ProducerRecord<>(requestTopic, String.valueOf(postId), request);

        return Mono.fromFuture(replyingKafkaTemplate.sendAndReceive(record))
                .timeout(Duration.ofSeconds(30))
                .map(ConsumerRecord::value)
                .doOnSuccess(response -> log.info("📨 [KAFKA] Received response - postId: {}, title: {}", 
                        postId, response.getTitle()))
                .doOnError(error -> log.error("❌ [KAFKA] Failed - postId: {}, error: {}", 
                        postId, error.getMessage()));
    }
}
