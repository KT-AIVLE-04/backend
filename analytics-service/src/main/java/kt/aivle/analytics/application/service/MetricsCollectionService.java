package kt.aivle.analytics.application.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelStatistics;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoStatistics;
import kt.aivle.analytics.application.port.in.MetricsCollectionUseCase;
import kt.aivle.analytics.domain.entity.AccountMetric;
import kt.aivle.analytics.domain.entity.Post;
import kt.aivle.analytics.domain.entity.PostCommentMetric;
import kt.aivle.analytics.domain.entity.PostMetric;
import kt.aivle.analytics.domain.entity.SnsAccount;
import kt.aivle.analytics.application.port.out.AccountMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.PostCommentMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.PostMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.PostRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsCollectionService implements MetricsCollectionUseCase {
    
    private final PostRepositoryPort postRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final PostMetricRepositoryPort postMetricRepositoryPort;
    private final PostCommentMetricRepositoryPort postCommentMetricRepositoryPort;
    private final AccountMetricRepositoryPort accountMetricRepositoryPort;
    private final BatchJobMonitor batchJobMonitor;
    
    @Value("${app.youtube.api.key}")
    private String youtubeApiKey;
    
    @Value("${app.youtube.api.quota-limit:10000}")
    private int quotaLimit;
    
    @Value("${app.youtube.api.quota-window:86400}")
    private int quotaWindow;
    
    @Value("${app.youtube.api.batch-size:100}")
    private int batchSize;
    
    @Value("${app.youtube.api.retry-attempts:3}")
    private int retryAttempts;
    
    @Value("${app.youtube.api.retry-delay:1000}")
    private int retryDelay;
    
    private final AtomicInteger apiCallCount = new AtomicInteger(0);
    private final LocalDateTime quotaResetTime = LocalDateTime.now().plusSeconds(quotaWindow);
    
    @Override
    public void collectAccountMetrics() {
        String jobName = "account-metrics-collection";
        batchJobMonitor.recordJobStart(jobName);
        
        log.info("Starting account metrics collection for all accounts with batch size: {}", batchSize);
        
        try {
            int page = 0;
            int totalProcessed = 0;
            int totalAccounts = 0;
            
            while (true) {
                List<SnsAccount> snsAccounts = snsAccountRepositoryPort.findAllWithPagination(page, batchSize);
                if (snsAccounts.isEmpty()) break;
                
                totalAccounts += snsAccounts.size();
                
                for (SnsAccount snsAccount : snsAccounts) {
                    try {
                        if (checkQuotaLimit()) {
                            collectAccountMetricsByAccountId(snsAccount.getId());
                            totalProcessed++;
                            
                            // 진행률 업데이트
                            batchJobMonitor.recordJobProgress(jobName, totalProcessed, totalAccounts);
                        } else {
                            log.warn("YouTube API quota limit reached. Stopping collection.");
                            break;
                        }
                    } catch (Exception e) {
                        log.error("Failed to collect metrics for account: {}", snsAccount.getId(), e);
                    }
                }
                
                page++;
                
                // 배치 간 잠시 대기 (API 호출 제한 고려)
                Thread.sleep(100);
            }
            
            batchJobMonitor.recordJobSuccess(jobName);
            log.info("Completed account metrics collection for {} accounts", totalProcessed);
            
        } catch (Exception e) {
            batchJobMonitor.recordJobFailure(jobName, e.getMessage());
            log.error("Failed to collect account metrics", e);
            throw new RuntimeException("Failed to collect account metrics", e);
        }
    }
    
    @Override
    public void collectPostMetrics() {
        String jobName = "post-metrics-collection";
        batchJobMonitor.recordJobStart(jobName);
        
        log.info("Starting post metrics collection for all posts with batch size: {}", batchSize);
        
        try {
            int page = 0;
            int totalProcessed = 0;
            int totalPosts = 0;
            
            while (true) {
                List<Post> posts = postRepositoryPort.findAllWithPagination(page, batchSize);
                if (posts.isEmpty()) break;
                
                totalPosts += posts.size();
                
                for (Post post : posts) {
                    try {
                        if (checkQuotaLimit()) {
                            collectPostMetricsByPostId(post.getId());
                            totalProcessed++;
                            
                            // 진행률 업데이트
                            batchJobMonitor.recordJobProgress(jobName, totalProcessed, totalPosts);
                        } else {
                            log.warn("YouTube API quota limit reached. Stopping collection.");
                            break;
                        }
                    } catch (Exception e) {
                        log.error("Failed to collect metrics for post: {}", post.getId(), e);
                    }
                }
                
                page++;
                
                // 배치 간 잠시 대기 (API 호출 제한 고려)
                Thread.sleep(100);
            }
            
            batchJobMonitor.recordJobSuccess(jobName);
            log.info("Completed post metrics collection for {} posts", totalProcessed);
            
        } catch (Exception e) {
            batchJobMonitor.recordJobFailure(jobName, e.getMessage());
            log.error("Failed to collect post metrics", e);
            throw new RuntimeException("Failed to collect post metrics", e);
        }
    }
    
    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void collectAccountMetricsByAccountId(Long accountId) {
        log.info("Collecting account metrics for accountId: {}", accountId);
        
        SnsAccount snsAccount = snsAccountRepositoryPort.findById(accountId)
            .orElseThrow(() -> new RuntimeException("SNS Account not found: " + accountId));
        
        if (!"YOUTUBE".equals(snsAccount.getType())) {
            log.warn("Skipping non-YouTube account: {}", accountId);
            return;
        }
        
        try {
            YouTube youtube = createYouTubeClient();
            
            // 채널 정보 조회
            ChannelListResponse channelResponse = youtube.channels()
                .list(Arrays.asList("statistics"))
                .setId(Arrays.asList(snsAccount.getSnsAccountId()))
                .setKey(youtubeApiKey)
                .execute();
            
            // API 호출 카운트 증가
            apiCallCount.incrementAndGet();
            
            if (channelResponse.getItems() != null && !channelResponse.getItems().isEmpty()) {
                Channel channel = channelResponse.getItems().get(0);
                ChannelStatistics statistics = channel.getStatistics();
                
                Long subscriberCount = statistics.getSubscriberCount() != null ? 
                    statistics.getSubscriberCount().longValue() : 0L;
                Long viewCount = statistics.getViewCount() != null ? 
                    statistics.getViewCount().longValue() : 0L;
                
                AccountMetric accountMetric = new AccountMetric(
                    snsAccount.getId(),
                    subscriberCount,
                    viewCount,
                    LocalDateTime.now()
                );
                
                accountMetricRepositoryPort.save(accountMetric);
                log.info("Saved account metrics for accountId: {}, subscribers: {}, views: {}", 
                    accountId, subscriberCount, viewCount);
            }
            
        } catch (IOException e) {
            log.error("Failed to collect account metrics for accountId: {}", accountId, e);
            throw new RuntimeException("Failed to collect account metrics", e);
        }
    }
    
    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void collectPostMetricsByPostId(Long postId) {
        log.info("Collecting post metrics for postId: {}", postId);
        
        Post post = postRepositoryPort.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        
        try {
            YouTube youtube = createYouTubeClient();
            
            // 비디오 정보 조회
            VideoListResponse videoResponse = youtube.videos()
                .list(Arrays.asList("statistics"))
                .setId(Arrays.asList(post.getSnsPostId()))
                .setKey(youtubeApiKey)
                .execute();
            
            // API 호출 카운트 증가
            apiCallCount.incrementAndGet();
            
            if (videoResponse.getItems() != null && !videoResponse.getItems().isEmpty()) {
                Video video = videoResponse.getItems().get(0);
                VideoStatistics statistics = video.getStatistics();
                
                String likeCount = statistics.getLikeCount() != null ? 
                    statistics.getLikeCount().toString() : "0";
                Long dislikeCount = statistics.getDislikeCount() != null ? 
                    statistics.getDislikeCount().longValue() : 0L;
                Long commentCount = statistics.getCommentCount() != null ? 
                    statistics.getCommentCount().longValue() : 0L;
                Long viewCount = statistics.getViewCount() != null ? 
                    statistics.getViewCount().longValue() : 0L;
                
                // YouTube API v3에서는 share 정보를 직접 제공하지 않으므로 null로 설정
                Long shareCount = null;
                
                PostMetric postMetric = new PostMetric(
                    post.getId(),
                    likeCount,
                    dislikeCount,
                    commentCount,
                    shareCount,
                    viewCount,
                    LocalDateTime.now()
                );
                
                postMetricRepositoryPort.save(postMetric);
                log.info("Saved post metrics for postId: {}, likes: {}, dislikes: {}, comments: {}, views: {}", 
                    postId, likeCount, dislikeCount, commentCount, viewCount);
            }
            
        } catch (IOException e) {
            log.error("Failed to collect post metrics for postId: {}", postId, e);
            throw new RuntimeException("Failed to collect post metrics", e);
        }
    }
    
    private boolean checkQuotaLimit() {
        // 할당량 리셋 시간이 지났으면 카운터 리셋
        if (LocalDateTime.now().isAfter(quotaResetTime)) {
            apiCallCount.set(0);
            log.info("YouTube API quota counter reset");
        }
        
        int currentCount = apiCallCount.get();
        if (currentCount >= quotaLimit) {
            log.warn("YouTube API quota limit reached: {}/{}", currentCount, quotaLimit);
            return false;
        }
        
        return true;
    }
    
    private YouTube createYouTubeClient() {
        return new YouTube.Builder(
            new com.google.api.client.http.javanet.NetHttpTransport(),
            new com.google.api.client.json.gson.GsonFactory(),
            null
        ).build();
    }
    
    @Override
    public void collectPostComments() {
        String jobName = "post-comments-collection";
        batchJobMonitor.recordJobStart(jobName);
        
        log.info("Starting post comments collection for all posts with batch size: {}", batchSize);
        
        try {
            int page = 0;
            int totalProcessed = 0;
            int totalPosts = 0;
            
            while (true) {
                List<Post> posts = postRepositoryPort.findAllWithPagination(page, batchSize);
                if (posts.isEmpty()) break;
                
                totalPosts += posts.size();
                
                for (Post post : posts) {
                    try {
                        if (checkQuotaLimit()) {
                            collectPostCommentsByPostId(post.getId());
                            totalProcessed++;
                            
                            // 진행률 업데이트
                            batchJobMonitor.recordJobProgress(jobName, totalProcessed, totalPosts);
                        } else {
                            log.warn("YouTube API quota limit reached. Stopping collection.");
                            break;
                        }
                    } catch (Exception e) {
                        log.error("Failed to collect comments for post: {}", post.getId(), e);
                    }
                }
                
                page++;
                Thread.sleep(100); // 배치 간 딜레이
            }
            
            batchJobMonitor.recordJobSuccess(jobName);
            log.info("Completed post comments collection for {} posts", totalProcessed);
            
        } catch (Exception e) {
            batchJobMonitor.recordJobFailure(jobName, e.getMessage());
            log.error("Failed to collect post comments", e);
            throw new RuntimeException("Failed to collect post comments", e);
        }
    }
    
    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void collectPostCommentsByPostId(Long postId) {
        log.info("Collecting post comments for postId: {}", postId);
        
        Post post = postRepositoryPort.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        
        try {
            YouTube youtube = createYouTubeClient();
            String nextPageToken = null;
            LocalDateTime lastCommentDate = LocalDateTime.now().minusDays(7); // 최근 7일 댓글만 수집
            
            do {
                // 댓글 스레드 조회
                CommentThreadListResponse commentResponse = youtube.commentThreads()
                    .list(Arrays.asList("snippet"))
                    .setVideoId(post.getSnsPostId())
                    .setMaxResults(100L)
                    .setOrder("time") // 최신순 정렬
                    .setPageToken(nextPageToken)
                    .setKey(youtubeApiKey)
                    .execute();
                
                apiCallCount.incrementAndGet();
                
                if (commentResponse.getItems() != null) {
                    for (CommentThread commentThread : commentResponse.getItems()) {
                        try {
                            // 댓글 작성일 확인
                            String publishedAtStr = commentThread.getSnippet().getTopLevelComment().getSnippet().getPublishedAt().toString();
                            LocalDateTime publishedAt = LocalDateTime.parse(publishedAtStr.substring(0, 19)); // ISO 8601 파싱
                            
                            // 최근 7일 이내 댓글만 처리
                            if (publishedAt.isAfter(lastCommentDate)) {
                                String commentId = commentThread.getSnippet().getTopLevelComment().getId();
                                String content = commentThread.getSnippet().getTopLevelComment().getSnippet().getTextDisplay();
                                
                                // 이미 수집된 댓글인지 확인
                                if (!postCommentMetricRepositoryPort.findBySnsCommentId(commentId).isPresent()) {
                                    PostCommentMetric commentMetric = new PostCommentMetric(
                                        commentId,
                                        post.getId(),
                                        content,
                                        LocalDateTime.now()
                                    );
                                    
                                    postCommentMetricRepositoryPort.save(commentMetric);
                                    log.debug("Saved comment for postId: {}, commentId: {}", postId, commentId);
                                }
                            } else {
                                // 오래된 댓글이 나오면 수집 중단
                                log.info("Reached old comments for postId: {}, stopping collection", postId);
                                return;
                            }
                        } catch (Exception e) {
                            log.error("Failed to process comment for postId: {}", postId, e);
                        }
                    }
                }
                
                nextPageToken = commentResponse.getNextPageToken();
                
            } while (nextPageToken != null);
            
            log.info("Completed comment collection for postId: {}", postId);
            
        } catch (IOException e) {
            log.error("Failed to collect comments for postId: {}", postId, e);
            throw new RuntimeException("Failed to collect comments", e);
        }
    }
}
