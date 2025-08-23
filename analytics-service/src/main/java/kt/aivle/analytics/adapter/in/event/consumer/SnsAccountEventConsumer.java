package kt.aivle.analytics.adapter.in.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent;
import kt.aivle.analytics.application.port.in.AnalyticsEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnsAccountEventConsumer {
    
    private final AnalyticsEventUseCase analyticsEventUseCase;
    
    @KafkaListener(topics = "sns-account.connected", containerFactory = "snsAccountEventListenerFactory")
    public void handleSnsAccountConnected(SnsAccountEvent event) {
        try {
            log.info("Received SNS account connected event: {}", event);
            
            analyticsEventUseCase.handleSnsAccountConnected(event);
            
        } catch (Exception e) {
            log.error("Failed to handle SNS account connected event: {}", e.getMessage(), e);
            // 예외를 던지지 않고 로깅만 함으로써 컨테이너가 멈추지 않도록 함
        }
    }
    
    @KafkaListener(topics = "sns-account.disconnected", containerFactory = "snsAccountEventListenerFactory")
    public void handleSnsAccountDisconnected(SnsAccountEvent event) {
        try {
            log.info("Received SNS account disconnected event: {}", event);
            
            analyticsEventUseCase.handleSnsAccountDisconnected(event);
            
        } catch (Exception e) {
            log.error("Failed to handle SNS account disconnected event: {}", e.getMessage(), e);
            // 예외를 던지지 않고 로깅만 함으로써 컨테이너가 멈추지 않도록 함
        }
    }
    

}
