package kt.aivle.analytics.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.event.dto.SnsAccountEvent;
import kt.aivle.analytics.adapter.in.event.dto.SnsPostEvent;
import kt.aivle.analytics.application.port.in.AnalyticsEventUseCase;
import kt.aivle.analytics.application.port.out.infrastructure.CachePort;
import kt.aivle.analytics.application.port.out.repository.SnsAccountRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsPostRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsAccount;
import kt.aivle.analytics.domain.entity.SnsPost;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEventService implements AnalyticsEventUseCase {

    private final SnsPostRepositoryPort snsPostRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final CachePort cachePort;
    
    @Override
    public void handlePostCreated(SnsPostEvent event) {
        try {
            log.info("Processing post created event: postId={}, accountId={}, snsPostId={}", 
                    event.getPostId(), event.getAccountId(), event.getSnsPostId());
            
            // 기존 포스트 확인
            Optional<SnsPost> existingPost = snsPostRepositoryPort.findById(event.getPostId());
            
            if (existingPost.isPresent()) {
                log.warn("Post already exists, updating: postId={}", event.getPostId());
                // 기존 데이터 업데이트
                SnsPost post = existingPost.get();
                // 필요한 경우 필드 업데이트 로직 추가
                snsPostRepositoryPort.save(post);
            } else {
                // 새 포스트 생성
                SnsPost post = SnsPost.builder()
                    .id(event.getPostId())
                    .accountId(event.getAccountId())
                    .snsPostId(event.getSnsPostId())
                    .build();
                snsPostRepositoryPort.save(post);
            }
            
            // 관련 캐시 무효화
            cachePort.evictPostCache(event.getPostId());
            cachePort.evictAccountCache(event.getAccountId());
            
            log.info("Post processed successfully: postId={}", event.getPostId());
            
        } catch (Exception e) {
            log.error("Failed to process post created event: {}", e.getMessage(), e);
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
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
                    cachePort.evictPostCache(post.getId());
                    cachePort.evictAccountCache(event.getAccountId());
                });
            
            log.info("Post deleted successfully: postId={}", event.getPostId());
            
        } catch (Exception e) {
            log.error("Failed to process post deleted event: {}", e.getMessage(), e);
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
        }
    }
    
    @Override
    public void handleSnsAccountConnected(SnsAccountEvent event) {
        try {
            log.info("Processing SNS account connected event: accountId={}, userId={}, snsAccountId={}, type={}", 
                    event.getAccountId(), event.getUserId(), event.getSnsAccountId(), event.getType());
            
            // 기존 계정 확인
            Optional<SnsAccount> existingAccount = snsAccountRepositoryPort.findById(event.getAccountId());
            
            if (existingAccount.isPresent()) {
                log.warn("SNS account already exists, updating: accountId={}", event.getAccountId());
                // 기존 데이터 업데이트 (필요한 경우 필드 업데이트 로직 추가)
                SnsAccount account = existingAccount.get();
                snsAccountRepositoryPort.save(account);
            } else {
                // 새 계정 생성
                SnsAccount snsAccount = SnsAccount.builder()
                    .id(event.getAccountId())
                    .userId(event.getUserId())
                    .snsAccountId(event.getSnsAccountId())
                    .type(event.getType())
                    .build();
                snsAccountRepositoryPort.save(snsAccount);
            }
            
            // 관련 캐시 무효화
            cachePort.evictAccountCache(event.getAccountId());
            
            log.info("SNS account processed successfully: accountId={}", event.getAccountId());
            
        } catch (Exception e) {
            log.error("Failed to process SNS account connected event: {}", e.getMessage(), e);
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
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
                    cachePort.evictAccountCache(account.getId());
                });
            
            log.info("SNS account deleted successfully: accountId={}", event.getAccountId());
            
        } catch (Exception e) {
            log.error("Failed to process SNS account disconnected event: {}", e.getMessage(), e);
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
        }
    }
}
