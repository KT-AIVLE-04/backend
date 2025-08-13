package kt.aivle.analytics.adapter.in.event.consumer;

import static kt.aivle.analytics.exception.AnalyticsErrorCode.KAFKA_ERROR;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.adapter.out.event.SnsTokenResponseEvent;
import kt.aivle.analytics.application.port.in.AnalyticsEventUseCase;
import kt.aivle.common.exception.InfraException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnsTokenResponseEventConsumer {
    
    private final AnalyticsEventUseCase analyticsEventUseCase;
    
    @KafkaListener(topics = "sns-token.response", groupId = "analytics-service")
    public void handleSnsTokenResponse(SnsTokenResponseEvent event) {
        try {
            log.info("Received SNS token response: requestId={}, userId={}, snsType={}, isExpired={}", 
                    event.requestId(), event.userId(), event.snsType(), event.isExpired());
            
            analyticsEventUseCase.handleSnsTokenResponse(event);
        } catch (Exception e) {
            log.error("SNS 토큰 응답 처리 실패", e);
            throw new InfraException(KAFKA_ERROR, e);
        }
    }
}
