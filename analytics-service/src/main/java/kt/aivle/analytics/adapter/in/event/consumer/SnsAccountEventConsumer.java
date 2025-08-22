package kt.aivle.analytics.adapter.in.event.consumer;

import kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent;
import kt.aivle.analytics.application.port.in.AnalyticsEventUseCase;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnsAccountEventConsumer {
    
    private final AnalyticsEventUseCase analyticsEventUseCase;
    
    @KafkaListener(topics = "sns-account.connected", groupId = "analytics-service")
    public void handleSnsAccountConnected(SnsAccountEvent event) {
        try {
            log.info("Received SNS account connected event: accountId={}, userId={}, snsAccountId={}, type={}", 
                    event.getAccountId(), event.getUserId(), event.getSnsAccountId(), event.getType());
            
            analyticsEventUseCase.handleSnsAccountConnected(event);
            
        } catch (Exception e) {
            log.error("Failed to handle SNS account connected event: {}", e.getMessage(), e);
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
        }
    }
    
    @KafkaListener(topics = "sns-account.disconnected", groupId = "analytics-service")
    public void handleSnsAccountDisconnected(SnsAccountEvent event) {
        try {
            log.info("Received SNS account disconnected event: accountId={}, userId={}, snsAccountId={}, type={}", 
                    event.getAccountId(), event.getUserId(), event.getSnsAccountId(), event.getType());
            
            analyticsEventUseCase.handleSnsAccountDisconnected(event);
            
        } catch (Exception e) {
            log.error("Failed to handle SNS account disconnected event: {}", e.getMessage(), e);
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
        }
    }
}
