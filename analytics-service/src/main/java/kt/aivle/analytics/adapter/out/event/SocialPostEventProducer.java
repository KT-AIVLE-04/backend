package kt.aivle.analytics.adapter.out.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.out.event.dto.SocialPostRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialPostEventProducer {
    
    private final KafkaTemplate<String, SocialPostRequestEvent> kafkaTemplate;
    private static final String TOPIC = "social-post.request";
    
    public void requestSocialPosts(SocialPostRequestEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.getRequestId(), event);
            log.info("Social post request event sent: requestId={}, userId={}, snsType={}", 
                    event.getRequestId(), event.getUserId(), event.getSnsType());
        } catch (Exception e) {
            log.error("Failed to send social post request event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send social post request event", e);
        }
    }
}
