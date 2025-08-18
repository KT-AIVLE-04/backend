package kt.aivle.analytics.application.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import kt.aivle.analytics.application.port.in.MetricsCollectionUseCase;
import kt.aivle.analytics.application.port.out.SnsAccountMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsPostCommentMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsPostMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsPostRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsAccount;
import kt.aivle.analytics.domain.entity.SnsAccountMetric;
import kt.aivle.analytics.domain.entity.SnsPost;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.domain.entity.SnsPostMetric;
import kt.aivle.analytics.domain.model.SnsType;
import kt.aivle.analytics.exception.AnalyticsException;
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
    private final YouTubeApiQuotaManager quotaManager;
    private final MetricsValidator metricsValidator;
    private final YouTubeApiService youtubeApiService;
    
    @Value("${app.youtube.api.batch-size:100}")
    private int batchSize;
    
    @Value("${app.youtube.api.retry-attempts:3}")
    private int retryAttempts;
    
    @Value("${app.youtube.api.retry-delay:1000}")
    private int retryDelay;
    
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
                        if (quotaManager.checkQuotaLimit(YouTubeApiQuotaManager.ApiPriority.BATCH_COLLECTION)) {
                            itemProcessor.accept(idExtractor.apply(item));
                            totalProcessed++;
                            batchJobMonitor.recordJobProgress(jobName, totalProcessed, (int) totalItems);
                        } else {
                            log.warn("YouTube API quota limit reached. Stopping collection.");
                            break;
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
            throw new AnalyticsException("Failed to collect " + itemType, e);
        }
    }
    
    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void collectAccountMetricsByAccountId(Long accountId) {
        log.info("Collecting account metrics for accountId: {}", accountId);
        
        SnsAccount snsAccount = snsAccountRepositoryPort.findById(accountId)
            .orElseThrow(() -> new AnalyticsException("SNS Account not found: " + accountId));
        
        if (snsAccount.getType() != SnsType.YOUTUBE) {
            log.warn("Skipping non-YouTube account: {}", accountId);
            return;
        }
        
        try {
            // 채널 정보 조회
            var statistics = youtubeApiService.getChannelStatistics(snsAccount.getSnsAccountId());
                
            // API 응답 검증
            if (statistics == null) {
                log.warn("Channel statistics is null for accountId: {}", accountId);
                return;
            }
            
            Long subscriberCount = statistics.getSubscriberCount() != null ? 
                statistics.getSubscriberCount().longValue() : 0L;
            Long viewCount = statistics.getViewCount() != null ? 
                statistics.getViewCount().longValue() : 0L;
            
            // 데이터 유효성 검증
            metricsValidator.validateSubscriberCount(subscriberCount, accountId);
            metricsValidator.validateViewCount(viewCount, accountId, "account");
            
            // 중복 데이터 방지 - 최근 1시간 내 데이터가 있으면 스킵 (최적화)
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            boolean hasRecentData = snsAccountMetricRepositoryPort
                .existsByAccountIdAndCrawledAtAfter(snsAccount.getId(), oneHourAgo);
            
            if (hasRecentData) {
                log.info("Recent metrics already exist for accountId: {}, skipping", accountId);
                return;
            }
            
            SnsAccountMetric accountMetric = SnsAccountMetric.builder()
                .accountId(snsAccount.getId())
                .followers(subscriberCount)
                .views(viewCount)
                .crawledAt(LocalDateTime.now().withNano(0))
                .build();
            
            snsAccountMetricRepositoryPort.save(accountMetric);
            log.info("Saved account metrics for accountId: {}, subscribers: {}, views: {}", 
                accountId, subscriberCount, viewCount);
            
        } catch (Exception e) {
            log.error("Failed to collect account metrics for accountId: {}", accountId, e);
            throw new AnalyticsException("Failed to collect account metrics", e);
        }
    }
    
    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void collectPostMetricsByPostId(Long postId) {
        log.info("Collecting post metrics for postId: {}", postId);
        
        SnsPost post = snsPostRepositoryPort.findById(postId)
            .orElseThrow(() -> new AnalyticsException("Post not found: " + postId));
        
        try {
            // 비디오 정보 조회
            var statistics = youtubeApiService.getVideoStatistics(post.getSnsPostId());
            
            if (statistics != null) {
                // API 응답 검증
                if (statistics == null) {
                    log.warn("Video statistics is null for postId: {}", postId);
                    return;
                }
                
                String likeCount = statistics.getLikeCount() != null ? 
                    statistics.getLikeCount().toString() : "0";
                Long dislikeCount = statistics.getDislikeCount() != null ? 
                    statistics.getDislikeCount().longValue() : 0L;
                Long commentCount = statistics.getCommentCount() != null ? 
                    statistics.getCommentCount().longValue() : 0L;
                Long viewCount = statistics.getViewCount() != null ? 
                    statistics.getViewCount().longValue() : 0L;
                
                // 데이터 유효성 검증
                metricsValidator.validateViewCount(viewCount, postId, "post");
                metricsValidator.validateCommentCount(commentCount, postId);
                
                // YouTube API v3에서는 share 정보를 직접 제공하지 않으므로 null로 설정
                Long shareCount = null;
                
                // Long.parseLong() 예외 처리
                Long likes;
                try {
                    likes = Long.parseLong(likeCount);
                } catch (NumberFormatException e) {
                    log.warn("Invalid like count format for postId: {}, using 0", postId);
                    likes = 0L;
                }
                
                // 좋아요 수 유효성 검증
                metricsValidator.validateLikeCount(likes, postId);
                
                // 중복 데이터 방지 - 최근 1시간 내 데이터가 있으면 스킵 (최적화)
                LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                boolean hasRecentData = snsPostMetricRepositoryPort
                    .existsByPostIdAndCrawledAtAfter(post.getId(), oneHourAgo);
                
                if (hasRecentData) {
                    log.info("Recent metrics already exist for postId: {}, skipping", postId);
                    return;
                }
                
                SnsPostMetric postMetric = SnsPostMetric.builder()
                    .postId(post.getId())
                    .likes(likes)
                    .dislikes(dislikeCount)
                    .comments(commentCount)
                    .shares(shareCount)
                    .views(viewCount)
                    .crawledAt(LocalDateTime.now().withNano(0))
                    .build();
                
                snsPostMetricRepositoryPort.save(postMetric);
                log.info("Saved post metrics for postId: {}, likes: {}, dislikes: {}, comments: {}, views: {}", 
                    postId, likeCount, dislikeCount, commentCount, viewCount);
            }
            
        } catch (Exception e) {
            log.error("Failed to collect post metrics for postId: {}", postId, e);
            throw new AnalyticsException("Failed to collect post metrics", e);
        }
    }
    
    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void collectPostCommentsByPostId(Long postId) {
        log.info("Collecting post comments for postId: {}", postId);
        
        SnsPost post = snsPostRepositoryPort.findById(postId)
            .orElseThrow(() -> new AnalyticsException("Post not found: " + postId));
        
        try {
            YouTube youtube = new YouTube.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                null
            ).build();
            
            String nextPageToken = null;
            int newCommentsCount = 0;
            int totalProcessedCount = 0;
            
            do {
                // 댓글 스레드 조회 (시간순 정렬)
                CommentThreadListResponse commentResponse = youtube.commentThreads()
                    .list(Arrays.asList("snippet"))
                    .setVideoId(post.getSnsPostId())
                    .setMaxResults(100L)
                    .setOrder("time") // 최신순 정렬
                    .setPageToken(nextPageToken)
                    .setKey(youtubeApiService.getApiKey())
                    .execute();
                
                quotaManager.incrementApiCall();
                
                if (commentResponse.getItems() != null) {
                    for (CommentThread commentThread : commentResponse.getItems()) {
                        try {
                            totalProcessedCount++;
                            String commentId = commentThread.getSnippet().getTopLevelComment().getId();
                            
                            // 이미 DB에 있는 댓글인지 확인
                            if (snsPostCommentMetricRepositoryPort.findBySnsCommentId(commentId).isPresent()) {
                                log.info("Comment already exists in DB - commentId: {}, stopping collection. Total processed: {}, New comments: {}", 
                                    commentId, totalProcessedCount, newCommentsCount);
                                return; // 이미 있는 댓글을 만나면 수집 중단
                            }
                            
                            // 새로운 댓글 처리
                            String content = commentThread.getSnippet().getTopLevelComment().getSnippet().getTextDisplay();
                            String publishedAtStr = commentThread.getSnippet().getTopLevelComment().getSnippet().getPublishedAt().toString();
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(publishedAtStr);
                            LocalDateTime publishedAt = zonedDateTime.toLocalDateTime();
                            
                            log.info("Collecting new comment - commentId: {}, publishedAt: {}, content: {}", 
                                commentId, publishedAt, content);
                            
                            SnsPostCommentMetric commentMetric = SnsPostCommentMetric.builder()
                                .snsCommentId(commentId)
                                .postId(post.getId())
                                .content(content)
                                .crawledAt(LocalDateTime.now().withNano(0))
                                .build();
                            
                            snsPostCommentMetricRepositoryPort.save(commentMetric);
                            newCommentsCount++;
                            log.debug("Saved new comment for postId: {}, commentId: {}", postId, commentId);
                            
                        } catch (Exception e) {
                            log.error("Failed to process comment for postId: {}", postId, e);
                        }
                    }
                }
                
                nextPageToken = commentResponse.getNextPageToken();
                
            } while (nextPageToken != null);
            
            log.info("Completed comment collection for postId: {}. Total processed: {}, New comments: {}", 
                postId, totalProcessedCount, newCommentsCount);
            
        } catch (IOException e) {
            log.error("Failed to collect comments for postId: {}", postId, e);
            throw new AnalyticsException("Failed to collect comments", e);
        }
    }
}

