package kt.aivle.analytics.adapter.in.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.in.event.dto.SocialPostResponseEvent;
import kt.aivle.analytics.application.port.in.AnalyticsEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialPostResponseEventConsumer {
    
    private final AnalyticsEventUseCase analyticsEventUseCase;
    
    @KafkaListener(topics = "social-post.response", groupId = "analytics-service")
    public void handleSocialPostResponse(SocialPostResponseEvent event) {
        try {
            log.info("Received social post response event: requestId={}, userId={}, postsCount={}", 
                    event.getRequestId(), event.getUserId(), 
                    event.getPosts() != null ? event.getPosts().size() : 0);
            
            analyticsEventUseCase.handleSocialPostResponse(event);
            
        } catch (Exception e) {
            log.error("Failed to handle social post response event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to handle social post response event", e);
        }
    }
}
