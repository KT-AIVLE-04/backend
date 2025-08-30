package kt.aivle.analytics.adapter.out.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.in.event.dto.PostInfoRequestMessage;
import kt.aivle.analytics.adapter.in.event.dto.PostInfoResponseMessage;
import kt.aivle.analytics.application.port.out.SnsServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnsServiceAdapter implements SnsServicePort {

    private final ReplyingKafkaTemplate<String, PostInfoRequestMessage, PostInfoResponseMessage> replyingKafkaTemplate;

    private final String requestTopic = "post-info.request";

    @Override
    public CompletableFuture<PostInfoResponseMessage> getPostInfo(Long postId, Long userId, Long accountId, Long storeId) {
        log.info("📤 [KAFKA] Sending request - postId: {}", postId);
        
        PostInfoRequestMessage request = PostInfoRequestMessage.builder()
                .postId(postId)
                .userId(userId)
                .accountId(accountId)
                .storeId(storeId)
                .build();

        ProducerRecord<String, PostInfoRequestMessage> record =
                new ProducerRecord<>(requestTopic, String.valueOf(postId), request);

        return CompletableFuture.supplyAsync(() -> {
            try {
                ConsumerRecord<String, PostInfoResponseMessage> response = 
                    replyingKafkaTemplate.sendAndReceive(record).get(60, TimeUnit.SECONDS);
                
                log.info("📨 [KAFKA] Received response - postId: {}, title: {}", 
                        postId, response.value().getTitle());
                
                return response.value();
            } catch (Exception e) {
                log.error("❌ [KAFKA] Failed - postId: {}, error: {}", postId, e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
