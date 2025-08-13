package kt.aivle.analytics.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.event.dto.SocialPostResponseEvent;
import kt.aivle.analytics.adapter.out.event.SnsTokenEventProducer;
import kt.aivle.analytics.adapter.out.event.SnsTokenResponseEvent;
import kt.aivle.analytics.adapter.out.event.SocialPostEventProducer;
import kt.aivle.analytics.application.port.in.AnalyticsEventUseCase;
import kt.aivle.analytics.application.port.in.AnalyticsUseCase;
import kt.aivle.analytics.application.port.in.command.CollectMetricsCommand;
import kt.aivle.analytics.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEventService implements AnalyticsEventUseCase {

    private final SnsTokenEventProducer snsTokenEventProducer;
    private final SocialPostEventProducer socialPostEventProducer;
    private final AnalyticsUseCase analyticsUseCase;

    @Override
    public void handleSnsTokenResponse(SnsTokenResponseEvent event) {
        try {
            log.info("Processing SNS token response for userId: {}, snsType: {}, isExpired: {}", 
                    event.userId(), event.snsType(), event.isExpired());
            
            // 토큰 응답을 받았으므로 이제 YouTube API 호출 가능
            // TODO: 토큰을 저장하고 메트릭 수집 로직에서 사용
            // TODO: 토큰이 만료된 경우 갱신 요청
            
        } catch (Exception e) {
            log.error("Failed to process SNS token response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process SNS token response", e);
        }
    }
    
    @Override
    public void handleSocialPostResponse(SocialPostResponseEvent event) {
        try {
            log.info("Processing social post response for userId: {}, postsCount: {}", 
                    event.getUserId(), event.getPosts().size());
            
            // 받은 게시글 정보를 기반으로 메트릭 수집 시작
            CollectMetricsCommand command = CollectMetricsCommand.builder()
                .userId(event.getUserId())
                .snsType(SnsType.valueOf(event.getSnsType().toUpperCase()))
                .socialPosts(event.getPosts())
                .build();
            
            analyticsUseCase.collectMetrics(command);
            
        } catch (Exception e) {
            log.error("Failed to process social post response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process social post response", e);
        }
    }
    
    public void requestSocialPosts(String userId, SnsType snsType, LocalDate startDate, LocalDate endDate) {
        String requestId = UUID.randomUUID().toString();
        
        kt.aivle.analytics.adapter.out.event.dto.SocialPostRequestEvent event = 
            kt.aivle.analytics.adapter.out.event.dto.SocialPostRequestEvent.builder()
                .requestId(requestId)
                .userId(userId)
                .snsType(snsType.name().toLowerCase())
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .build();
        
        socialPostEventProducer.requestSocialPosts(event);
        log.info("Requested social posts for userId: {}, snsType: {}, dateRange: {} to {}", 
                userId, snsType, startDate, endDate);
    }
}
