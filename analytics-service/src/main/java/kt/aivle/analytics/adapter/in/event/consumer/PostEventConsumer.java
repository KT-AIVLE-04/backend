package kt.aivle.analytics.adapter.in.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.in.event.dto.PostEvent;
import kt.aivle.analytics.application.port.in.AnalyticsEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventConsumer {
    
    private final AnalyticsEventUseCase analyticsEventUseCase;
    
    @KafkaListener(topics = "post.created", groupId = "analytics-service")
    public void handlePostCreated(PostEvent event) {
        try {
            log.info("Received post created event: postId={}, accountId={}, snsPostId={}", 
                    event.getPostId(), event.getAccountId(), event.getSnsPostId());
            
            analyticsEventUseCase.handlePostCreated(event);
            
        } catch (Exception e) {
            log.error("Failed to handle post created event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to handle post created event", e);
        }
    }
    
    @KafkaListener(topics = "post.deleted", groupId = "analytics-service")
    public void handlePostDeleted(PostEvent event) {
        try {
            log.info("Received post deleted event: postId={}, accountId={}, snsPostId={}", 
                    event.getPostId(), event.getAccountId(), event.getSnsPostId());
            
            analyticsEventUseCase.handlePostDeleted(event);
            
        } catch (Exception e) {
            log.error("Failed to handle post deleted event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to handle post deleted event", e);
        }
    }
}
