package kt.aivle.analytics.application.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.application.port.in.MetricsCollectionUseCase;
import kt.aivle.analytics.application.port.out.infrastructure.ExternalApiPort;
import kt.aivle.analytics.application.port.out.infrastructure.ValidationPort;
import kt.aivle.analytics.application.port.out.infrastructure.ValidationPort.MetricsData;
import kt.aivle.analytics.application.port.out.repository.SnsAccountMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsAccountRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsPostCommentMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsPostMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsPostRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsAccount;
import kt.aivle.analytics.domain.entity.SnsAccountMetric;
import kt.aivle.analytics.domain.entity.SnsPost;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.domain.entity.SnsPostMetric;
import kt.aivle.analytics.domain.model.SnsType;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsCollectionService implements MetricsCollectionUseCase {
    
    private final SnsPostRepositoryPort snsPostRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final SnsPostMetricRepositoryPort snsPostMetricRepositoryPort;
    private final SnsPostCommentMetricRepositoryPort snsPostCommentMetricRepositoryPort;
    private final SnsAccountMetricRepositoryPort snsAccountMetricRepositoryPort;
    private final BatchJobMonitor batchJobMonitor;
    private final ValidationPort validationPort;
    private final ExternalApiPort externalApiPort;
    private final EmotionAnalysisService emotionAnalysisService;
    
    @Value("${app.youtube.api.batch-size:100}")
    private int batchSize;
    
    @Value("${app.youtube.api.retry-attempts:3}")
    private int retryAttempts;
    
    @Value("${app.youtube.api.retry-delay:1000}")
    private int retryDelay;
    
    @Value("${app.youtube.api.parallel-threads:4}")
    private int parallelThreads;
    
    @Override
    public void collectAccountMetrics() {
        processBatch(
            "account-metrics-collection",
            snsAccountRepositoryPort::countAll,
            page -> snsAccountRepositoryPort.findAllWithPagination(page, batchSize),
            SnsAccount::getId,
            this::collectAccountMetricsByAccountId,
            "accounts"
        );
    }
    
    @Override
    public void collectPostMetrics() {
        processBatch(
            "post-metrics-collection",
            snsPostRepositoryPort::countAll,
            page -> snsPostRepositoryPort.findAllWithPagination(page, batchSize),
            SnsPost::getId,
            this::collectPostMetricsByPostId,
            "posts"
        );
    }
    
    @Override
    public void collectPostComments() {
        processBatch(
            "post-comments-collection",
            snsPostRepositoryPort::countAll,
            page -> snsPostRepositoryPort.findAllWithPagination(page, batchSize),
            SnsPost::getId,
            this::collectPostCommentsByPostId,
            "posts"
        );
    }
    
    // 제네릭 배치 처리 메서드
    private <T> void processBatch(
        String jobName,
        java.util.function.Supplier<Long> countSupplier,
        java.util.function.Function<Integer, List<T>> pageSupplier,
        java.util.function.Function<T, Long> idExtractor,
        java.util.function.Consumer<Long> itemProcessor,
        String itemType
    ) {
        batchJobMonitor.recordJobStart(jobName);
        
        try {
            int page = 0;
            int totalProcessed = 0;
            List<Long> failedIds = new ArrayList<>();
            
            long totalItems = countSupplier.get();
            log.info("Starting {} collection for all {} with batch size: {}", itemType, itemType, batchSize);
            log.info("Total {} to process: {}", itemType, totalItems);
            
            while (true) {
                List<T> items = pageSupplier.apply(page);
                if (items.isEmpty()) break;
                
                for (T item : items) {
                    try {
                        itemProcessor.accept(idExtractor.apply(item));
                        totalProcessed++;
                        batchJobMonitor.recordJobProgress(jobName, totalProcessed, (int) totalItems);
                        
                    } catch (BusinessException e) {
                        if (e.getMessage().contains("할당량")) {
                            log.warn("YouTube API quota exceeded during {} collection. Stopping batch.", itemType);
                            // 할당량 초과 시 배치 작업 중단
                            break;
                        } else {
                            failedIds.add(idExtractor.apply(item));
                            log.error("Failed to collect {} for {}: {}", itemType, idExtractor.apply(item), e);
                        }
                    } catch (Exception e) {
                        failedIds.add(idExtractor.apply(item));
                        log.error("Failed to collect {} for {}: {}", itemType, idExtractor.apply(item), e);
                    }
                }
                page++;
            }
            
            if (!failedIds.isEmpty()) {
                log.warn("Failed to collect {} for {} {}: {}", itemType, failedIds.size(), itemType, failedIds);
            }
            
            batchJobMonitor.recordJobSuccess(jobName);
            log.info("Completed {} collection for {} {}", itemType, totalProcessed, itemType);
            
        } catch (Exception e) {
            batchJobMonitor.recordJobFailure(jobName, e.getMessage());
            log.error("Failed to collect {}", itemType, e);
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
        }
    }
    
    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void collectAccountMetricsByAccountId(Long accountId) {
        log.info("Collecting account metrics for accountId: {}", accountId);
        
        SnsAccount snsAccount = snsAccountRepositoryPort.findById(accountId)
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.ACCOUNT_NOT_FOUND));
        
        if (snsAccount.getType() != SnsType.YOUTUBE) {
            log.warn("Skipping non-YouTube account: {}", accountId);
            return;
        }
        
        try {
            // 채널 정보 조회
            var statistics = externalApiPort.getChannelStatistics(snsAccount.getSnsAccountId());
                
            // API 응답 검증
            if (statistics == null) {
                log.warn("Channel statistics is null for accountId: {}", accountId);
                return;
            }
            
            Long subscriberCount = statistics.getSubscriberCount();
            Long viewCount = statistics.getViewCount();
            
            // 데이터 유효성 검증
            MetricsData metricsData = new MetricsData(subscriberCount, viewCount, null, null, "account", accountId);
            validationPort.validateMetrics(metricsData);
            
            // 중복 데이터 방지 - 최근 1시간 내 데이터가 있으면 스킵 (최적화)
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            boolean hasRecentData = snsAccountMetricRepositoryPort
                .existsByAccountIdAndCreatedAtAfter(snsAccount.getId(), oneHourAgo);
            
            if (hasRecentData) {
                log.info("Recent metrics already exist for accountId: {}, skipping", accountId);
                return;
            }
            
            SnsAccountMetric accountMetric = SnsAccountMetric.builder()
                .accountId(snsAccount.getId())
                .followers(subscriberCount)
                .views(viewCount)
                .build();
            
            snsAccountMetricRepositoryPort.save(accountMetric);
            log.info("Saved account metrics for accountId: {}, subscribers: {}, views: {}", 
                accountId, subscriberCount, viewCount);
            
        } catch (Exception e) {
            log.error("Failed to collect account metrics for accountId: {}", accountId, e);
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
        }
    }
    
    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void collectPostMetricsByPostId(Long postId) {
        log.info("Collecting post metrics for postId: {}", postId);
        
        SnsPost post = snsPostRepositoryPort.findById(postId)
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND));
        
        try {
            // 비디오 정보 조회
            var statistics = externalApiPort.getVideoStatistics(post.getSnsPostId());
            
            if (statistics != null) {
                Long likeCount = statistics.getLikeCount();
                Long dislikeCount = 0L; // YouTube API v3에서는 dislike count를 제공하지 않음
                Long commentCount = statistics.getCommentCount();
                Long viewCount = statistics.getViewCount();
                
                // 데이터 유효성 검증
                MetricsData metricsData = new MetricsData(null, viewCount, likeCount, commentCount, "post", postId);
                validationPort.validateMetrics(metricsData);
                
                // YouTube API v3에서는 share 정보를 직접 제공하지 않으므로 null로 설정
                Long shareCount = null;
                
                // 중복 데이터 방지 - 최근 1시간 내 데이터가 있으면 스킵 (최적화)
                LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                boolean hasRecentData = snsPostMetricRepositoryPort
                    .existsByPostIdAndCreatedAtAfter(post.getId(), oneHourAgo);
                
                if (hasRecentData) {
                    log.info("Recent metrics already exist for postId: {}, skipping", postId);
                    return;
                }
                
                SnsPostMetric postMetric = SnsPostMetric.builder()
                    .postId(post.getId())
                    .likes(likeCount)
                    .dislikes(dislikeCount)
                    .comments(commentCount)
                    .shares(shareCount)
                    .views(viewCount)
                    .build();
                
                snsPostMetricRepositoryPort.save(postMetric);
                log.info("Saved post metrics for postId: {}, likes: {}, dislikes: {}, comments: {}, views: {}", 
                    postId, likeCount, dislikeCount, commentCount, viewCount);
            }
            
        } catch (Exception e) {
            log.error("Failed to collect post metrics for postId: {}", postId, e);
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
        }
    }
    
    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void collectPostCommentsByPostId(Long postId) {
        log.info("Collecting post comments for postId: {}", postId);
        
        SnsPost post = snsPostRepositoryPort.findById(postId)
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND));
        
        try {
            // 1. API 호출로 댓글 데이터 수집 (트랜잭션 외부)
            List<SnsPostCommentMetric> newComments = fetchCommentsFromAPI(post, postId);
            
            // 2. DB 저장 (별도 트랜잭션)
            if (!newComments.isEmpty()) {
                saveCommentsToDatabase(newComments, postId);
            }
            
        } catch (IOException e) {
            log.error("Failed to collect comments for postId: {}", postId, e);
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
        }
    }
    
    // API 호출 메서드 (트랜잭션 외부)
    private List<SnsPostCommentMetric> fetchCommentsFromAPI(SnsPost post, Long postId) throws IOException {
        List<SnsPostCommentMetric> newComments = new ArrayList<>();
        
        // ExternalApiPort를 통해 댓글 조회
        List<PostCommentsQueryResponse> comments = externalApiPort.getVideoComments(post.getSnsPostId());
        
        for (PostCommentsQueryResponse comment : comments) {
            try {
                // 이미 DB에 있는 댓글인지 확인
                try {
                    if (snsPostCommentMetricRepositoryPort.findBySnsCommentId(comment.getCommentId()).isPresent()) {
                        log.info("Comment already exists in DB - commentId: {}, stopping collection. Total processed: {}", 
                            comment.getCommentId(), newComments.size());
                        return newComments; // 이미 있는 댓글을 만나면 수집 중단
                    }
                } catch (Exception e) {
                    log.warn("Failed to check existing comment for commentId: {}, continuing with collection", comment.getCommentId());
                    // 기존 댓글 확인 실패 시 계속 진행
                }
                
                // 새로운 댓글 처리
                String content = comment.getText();
                // 긴 댓글은 1000자로 제한 (DB TEXT 타입이지만 안전하게)
                if (content != null && content.length() > 1000) {
                    content = content.substring(0, 1000);
                    log.warn("Comment content truncated to 1000 characters for commentId: {}", comment.getCommentId());
                }
                
                log.info("Collecting new comment - commentId: {}, publishedAt: {}, content: {}", 
                    comment.getCommentId(), comment.getPublishedAt(), content);
                
                SnsPostCommentMetric commentMetric = SnsPostCommentMetric.builder()
                    .snsCommentId(comment.getCommentId())
                    .postId(post.getId())
                    .authorId(comment.getAuthorId())  // null 가능
                    .content(content)
                    .likeCount(comment.getLikeCount())
                    .publishedAt(comment.getPublishedAt())
                    .build();
                
                newComments.add(commentMetric);
                
            } catch (Exception e) {
                log.error("Failed to process comment for postId: {}", postId, e);
            }
        }
        
        log.info("Completed comment collection for postId: {}. Total processed: {}, New comments: {}", 
            postId, comments.size(), newComments.size());
        
        return newComments;
    }
    
    // DB 저장 메서드 (개별 저장으로 변경하여 일부 실패해도 계속 진행)
    private void saveCommentsToDatabase(List<SnsPostCommentMetric> newComments, Long postId) {
        log.info("Saving {} new comments to database for postId: {}", newComments.size(), postId);
        
        int savedCount = 0;
        for (SnsPostCommentMetric commentMetric : newComments) {
            try {
                // 개별 댓글을 별도 트랜잭션으로 저장
                SnsPostCommentMetric savedComment = saveCommentInTransaction(commentMetric);
                if (savedComment != null && savedComment.getId() != null) {
                    savedCount++;
                    log.debug("Saved new comment for postId: {}, commentId: {}", postId, commentMetric.getSnsCommentId());
                } else {
                    log.warn("Failed to save comment - saved entity is null or has no ID for commentId: {}", commentMetric.getSnsCommentId());
                }
            } catch (Exception e) {
                log.error("Failed to save comment for commentId: {}", commentMetric.getSnsCommentId(), e);
                // 개별 댓글 저장 실패는 다른 댓글 저장에 영향을 주지 않음
            }
        }
        
        log.info("Successfully saved {} out of {} comments for postId: {}", savedCount, newComments.size(), postId);
        
        // 새로운 댓글이 있으면 감정분석 수행 (AI 서버 미사용으로 주석 처리)
        if (savedCount > 0) {
            try {
                log.info("AI emotion analysis skipped for {} saved comments in postId: {} (AI server not available)", savedCount, postId);
                
                // 댓글을 PostCommentsQueryResponse로 변환
                List<PostCommentsQueryResponse> commentsForAnalysis = newComments.stream()
                    .map(comment -> PostCommentsQueryResponse.builder()
                        .commentId(comment.getSnsCommentId())
                        .authorId(comment.getAuthorId())
                        .text(comment.getContent())  // content -> text로 매핑
                        .likeCount(comment.getLikeCount())
                        .publishedAt(comment.getPublishedAt())
                        .build())
                    .collect(java.util.stream.Collectors.toList());
                
                // 감정분석 수행 (주석 처리)
                // emotionAnalysisService.analyzeAndSaveEmotions(postId, commentsForAnalysis);
                
                log.info("Emotion analysis skipped for postId: {} (AI server disabled)", postId);
                
            } catch (Exception e) {
                log.error("Failed to perform emotion analysis for postId: {}", postId, e);
                // 감정분석 실패는 댓글 수집을 중단시키지 않음
            }
        }
    }
    
    /**
     * 개별 댓글을 별도 트랜잭션으로 저장
     */
    @Transactional
    private SnsPostCommentMetric saveCommentInTransaction(SnsPostCommentMetric commentMetric) {
        return snsPostCommentMetricRepositoryPort.save(commentMetric);
    }
}

