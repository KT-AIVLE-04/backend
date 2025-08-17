package kt.aivle.analytics.application.service;

import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.event.dto.PostEvent;
import kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent;
import kt.aivle.analytics.application.port.in.AnalyticsEventUseCase;
import kt.aivle.analytics.application.port.out.PostRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.analytics.domain.entity.Post;
import kt.aivle.analytics.domain.entity.SnsAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEventService implements AnalyticsEventUseCase {

    private final PostRepositoryPort postRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    
    @Override
    public void handlePostCreated(PostEvent event) {
        try {
            log.info("Processing post created event: postId={}, accountId={}, snsPostId={}", 
                    event.getPostId(), event.getAccountId(), event.getSnsPostId());
            
            Post post = new Post(event.getAccountId(), event.getSnsPostId());
            postRepositoryPort.save(post);
            
            log.info("Post saved successfully: postId={}", event.getPostId());
            
        } catch (Exception e) {
            log.error("Failed to process post created event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process post created event", e);
        }
    }
    
    @Override
    public void handlePostDeleted(PostEvent event) {
        try {
            log.info("Processing post deleted event: postId={}, accountId={}, snsPostId={}", 
                    event.getPostId(), event.getAccountId(), event.getSnsPostId());
            
            postRepositoryPort.deleteBySnsPostId(event.getSnsPostId());
            
            log.info("Post deleted successfully: postId={}", event.getPostId());
            
        } catch (Exception e) {
            log.error("Failed to process post deleted event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process post deleted event", e);
        }
    }
    
    @Override
    public void handleSnsAccountConnected(SnsAccountEvent event) {
        try {
            log.info("Processing SNS account connected event: accountId={}, userId={}, snsAccountId={}, type={}", 
                    event.getAccountId(), event.getUserId(), event.getSnsAccountId(), event.getType());
            
            SnsAccount snsAccount = new SnsAccount(event.getUserId(), event.getSnsAccountId(), event.getType());
            snsAccountRepositoryPort.save(snsAccount);
            
            log.info("SNS account saved successfully: accountId={}", event.getAccountId());
            
        } catch (Exception e) {
            log.error("Failed to process SNS account connected event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process SNS account connected event", e);
        }
    }
    
    @Override
    public void handleSnsAccountDisconnected(SnsAccountEvent event) {
        try {
            log.info("Processing SNS account disconnected event: accountId={}, userId={}, snsAccountId={}, type={}", 
                    event.getAccountId(), event.getUserId(), event.getSnsAccountId(), event.getType());
            
            snsAccountRepositoryPort.deleteBySnsAccountId(event.getSnsAccountId());
            
            log.info("SNS account deleted successfully: accountId={}", event.getAccountId());
            
        } catch (Exception e) {
            log.error("Failed to process SNS account disconnected event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process SNS account disconnected event", e);
        }
    }
}
