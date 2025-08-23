package kt.aivle.analytics.adapter.in.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.in.event.dto.SnsPostEvent;
import kt.aivle.analytics.application.port.in.AnalyticsEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventConsumer {
    
    private final AnalyticsEventUseCase analyticsEventUseCase;
    
    @KafkaListener(topics = "post.created", containerFactory = "postEventListenerFactory")
    public void handlePostCreated(SnsPostEvent event) {
        try {
            log.info("Received post created event: {}", event);
            
            analyticsEventUseCase.handlePostCreated(event);
            
        } catch (Exception e) {
            log.error("Failed to handle post created event: {}", e.getMessage(), e);
            // 예외를 던지지 않고 로깅만 함으로써 컨테이너가 멈추지 않도록 함
        }
    }
    
    @KafkaListener(topics = "post.deleted", containerFactory = "postEventListenerFactory")
    public void handlePostDeleted(SnsPostEvent event) {
        try {
            log.info("Received post deleted event: {}", event);
            
            analyticsEventUseCase.handlePostDeleted(event);
            
        } catch (Exception e) {
            log.error("Failed to handle post deleted event: {}", e.getMessage(), e);
            // 예외를 던지지 않고 로깅만 함으로써 컨테이너가 멈추지 않도록 함
        }
    }
    

}
