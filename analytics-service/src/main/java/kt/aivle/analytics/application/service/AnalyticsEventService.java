package kt.aivle.analytics.application.service;

import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent;
import kt.aivle.analytics.adapter.in.event.dto.SnsPostEvent;
import kt.aivle.analytics.application.port.in.AnalyticsEventUseCase;
import kt.aivle.analytics.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsPostRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsAccount;
import kt.aivle.analytics.domain.entity.SnsPost;
import kt.aivle.analytics.exception.AnalyticsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEventService implements AnalyticsEventUseCase {

    private final SnsPostRepositoryPort snsPostRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final AnalyticsCacheService cacheService;
    
    @Override
    public void handlePostCreated(SnsPostEvent event) {
        try {
            log.info("Processing post created event: postId={}, accountId={}, snsPostId={}", 
                    event.getPostId(), event.getAccountId(), event.getSnsPostId());
            
            SnsPost post = SnsPost.builder()
                .id(event.getPostId())
                .accountId(event.getAccountId())
                .snsPostId(event.getSnsPostId())
                .build();
            snsPostRepositoryPort.save(post);
            
            // 관련 캐시 무효화
            cacheService.evictPostCache(event.getPostId());
            cacheService.evictAccountCache(event.getAccountId());
            
            log.info("Post saved successfully: postId={}", event.getPostId());
            
        } catch (Exception e) {
            log.error("Failed to process post created event: {}", e.getMessage(), e);
            throw new AnalyticsException("Failed to process post created event", e);
        }
    }
    
    @Override
    public void handlePostDeleted(SnsPostEvent event) {
        try {
            log.info("Processing post deleted event: postId={}, accountId={}, snsPostId={}", 
                    event.getPostId(), event.getAccountId(), event.getSnsPostId());
            
            snsPostRepositoryPort.findBySnsPostId(event.getSnsPostId())
                .ifPresent(post -> {
                    snsPostRepositoryPort.deleteById(post.getId());
                    
                    // 관련 캐시 무효화
                    cacheService.evictPostCache(post.getId());
                    cacheService.evictAccountCache(event.getAccountId());
                });
            
            log.info("Post deleted successfully: postId={}", event.getPostId());
            
        } catch (Exception e) {
            log.error("Failed to process post deleted event: {}", e.getMessage(), e);
            throw new AnalyticsException("Failed to process post deleted event", e);
        }
    }
    
    @Override
    public void handleSnsAccountConnected(SnsAccountEvent event) {
        try {
            log.info("Processing SNS account connected event: accountId={}, userId={}, snsAccountId={}, type={}", 
                    event.getAccountId(), event.getUserId(), event.getSnsAccountId(), event.getType());
            
            SnsAccount snsAccount = SnsAccount.builder()
                .id(event.getAccountId())
                .userId(event.getUserId())
                .snsAccountId(event.getSnsAccountId())
                .type(event.getType())
                .build();
            snsAccountRepositoryPort.save(snsAccount);
            
            // 관련 캐시 무효화
            cacheService.evictAccountCache(event.getAccountId());
            
            log.info("SNS account saved successfully: accountId={}", event.getAccountId());
            
        } catch (Exception e) {
            log.error("Failed to process SNS account connected event: {}", e.getMessage(), e);
            throw new AnalyticsException("Failed to process SNS account connected event", e);
        }
    }
    
    @Override
    public void handleSnsAccountDisconnected(SnsAccountEvent event) {
        try {
            log.info("Processing SNS account disconnected event: accountId={}, userId={}, snsAccountId={}, type={}", 
                    event.getAccountId(), event.getUserId(), event.getSnsAccountId(), event.getType());
            
            snsAccountRepositoryPort.findById(event.getAccountId())
                .ifPresent(account -> {
                    snsAccountRepositoryPort.deleteById(account.getId());
                    
                    // 관련 캐시 무효화
                    cacheService.evictAccountCache(account.getId());
                });
            
            log.info("SNS account deleted successfully: accountId={}", event.getAccountId());
            
        } catch (Exception e) {
            log.error("Failed to process SNS account disconnected event: {}", e.getMessage(), e);
            throw new AnalyticsException("Failed to process SNS account disconnected event", e);
        }
    }
}
