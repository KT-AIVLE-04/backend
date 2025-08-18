package kt.aivle.analytics.application.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThreadListResponse;

import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
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
import kt.aivle.analytics.exception.AnalyticsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsQueryService implements AnalyticsQueryUseCase {
    
    private final SnsPostMetricRepositoryPort snsPostMetricRepositoryPort;
    private final SnsAccountMetricRepositoryPort snsAccountMetricRepositoryPort;
    private final SnsPostCommentMetricRepositoryPort snsPostCommentMetricRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final SnsPostRepositoryPort snsPostRepositoryPort;
    private final YouTubeApiQuotaManager quotaManager;
    private final YouTubeApiService youtubeApiService;
    
    @Override
    public List<PostMetricsQueryResponse> getPostMetrics(String userId, PostMetricsQueryRequest request) {
        log.info("Getting post metrics for userId: {}, dateRange: {}, accountId: {}, postId: {}", 
                userId, request.getDateRange(), request.getAccountId(), request.getPostId());
        
        // current인 경우 실시간 데이터 조회
        if ("current".equals(request.getDateRange())) {
            return getCurrentPostMetrics(userId, request);
        }
        
        LocalDateTime startDate = getStartDate(request.getDateRange());
        LocalDateTime endDate = LocalDateTime.now();
        
        List<SnsPostMetric> metrics;
        if (request.getPostId() != null) {
            // 특정 게시물의 메트릭 조회
            metrics = snsPostMetricRepositoryPort.findByPostIdAndCrawledAtBetween(
                Long.parseLong(request.getPostId()), startDate, endDate);
        } else if (request.getAccountId() != null) {
            // 특정 계정의 모든 게시물 메트릭 조회
            List<SnsPost> posts = snsPostRepositoryPort.findByAccountId(Long.parseLong(request.getAccountId()));
            metrics = posts.stream()
                .flatMap(post -> snsPostMetricRepositoryPort.findByPostIdAndCrawledAtBetween(post.getId(), startDate, endDate).stream())
                .collect(Collectors.toList());
        } else {
            // 사용자의 모든 계정 메트릭 조회
            List<Long> accountIds = snsAccountRepositoryPort.findByUserId(Long.parseLong(userId))
                .stream()
                .map(account -> account.getId())
                .collect(Collectors.toList());
            
            metrics = accountIds.stream()
                .flatMap(accountId -> {
                    List<SnsPost> posts = snsPostRepositoryPort.findByAccountId(accountId);
                    return posts.stream()
                        .flatMap(post -> snsPostMetricRepositoryPort.findByPostIdAndCrawledAtBetween(post.getId(), startDate, endDate).stream());
                })
                .collect(Collectors.toList());
        }
        
        return metrics.stream()
            .map(this::toSnsPostMetricsQueryResponse)
            .collect(Collectors.toList());
    }
    
    private List<PostMetricsQueryResponse> getCurrentPostMetrics(String userId, PostMetricsQueryRequest request) {
        try {
            if (request.getPostId() != null) {
                Long postId = Long.parseLong(request.getPostId());
                String cacheKey = quotaManager.generateMetricsCacheKey("post", request.getPostId(), request.getDateRange());
                
                // 캐시에서 먼저 확인
                PostMetricsQueryResponse cached = quotaManager.getFromMetricsCache(cacheKey, PostMetricsQueryResponse.class);
                if (cached != null) {
                    log.info("Returning cached metrics for postId: {}", postId);
                    return List.of(cached);
                }
                
                // 할당량 체크
                if (!quotaManager.checkQuotaLimit(YouTubeApiQuotaManager.ApiPriority.REAL_TIME_QUERY)) {
                    log.warn("YouTube API quota limit reached. Returning empty response for postId: {}", postId);
                    return List.of();
                }
                
                // 특정 게시물의 실시간 메트릭 조회
                SnsPost post = snsPostRepositoryPort.findById(postId)
                    .orElseThrow(() -> new AnalyticsException("Post not found: " + postId));
                
                // YouTube API로 실시간 메트릭 조회
                var statistics = youtubeApiService.getVideoStatistics(post.getSnsPostId());
                
                if (statistics != null) {
                    
                    // 응답 형식 통일: DB 데이터와 동일한 형식으로 변환
                    PostMetricsQueryResponse response = new PostMetricsQueryResponse(
                        postId,
                        post.getSnsPostId(),
                        post.getAccountId(),
                        statistics.getLikeCount() != null ? statistics.getLikeCount().toString() : "0",
                        statistics.getDislikeCount() != null ? statistics.getDislikeCount().longValue() : 0L,
                        statistics.getCommentCount() != null ? statistics.getCommentCount().longValue() : 0L,
                        null, // YouTube API v3에서는 share 정보를 직접 제공하지 않음
                        statistics.getViewCount() != null ? statistics.getViewCount().longValue() : 0L,
                        LocalDateTime.now()
                    );
                    
                    // 캐시에 저장
                    quotaManager.putToMetricsCache(cacheKey, response);
                    
                    return List.of(response);
                }
            }
            
            return List.of();
        } catch (AnalyticsException e) {
            log.error("Analytics error while getting current post metrics: {}", e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            log.error("Unexpected error while getting current post metrics", e);
            return List.of();
        }
    }
    
    @Override
    public List<AccountMetricsQueryResponse> getAccountMetrics(String userId, AccountMetricsQueryRequest request) {
        log.info("Getting account metrics for userId: {}, dateRange: {}, accountId: {}", 
                userId, request.getDateRange(), request.getAccountId());
        
        // current인 경우 실시간 데이터 조회
        if ("current".equals(request.getDateRange())) {
            return getCurrentAccountMetrics(userId, request);
        }
        
        LocalDateTime startDate = getStartDate(request.getDateRange());
        LocalDateTime endDate = LocalDateTime.now();
        
        List<SnsAccountMetric> metrics;
        if (request.getAccountId() != null) {
            // 특정 계정의 메트릭 조회
            metrics = snsAccountMetricRepositoryPort.findByAccountIdAndCrawledAtBetween(
                Long.parseLong(request.getAccountId()), startDate, endDate);
        } else {
            // 사용자의 모든 계정 메트릭 조회
            List<Long> accountIds = snsAccountRepositoryPort.findByUserId(Long.parseLong(userId))
                .stream()
                .map(account -> account.getId())
                .collect(Collectors.toList());
            
            metrics = accountIds.stream()
                .flatMap(accountId -> snsAccountMetricRepositoryPort.findByAccountIdAndCrawledAtBetween(accountId, startDate, endDate).stream())
                .collect(Collectors.toList());
        }
        
        return metrics.stream()
            .map(this::toSnsAccountMetricsQueryResponse)
            .collect(Collectors.toList());
    }
    
    private List<AccountMetricsQueryResponse> getCurrentAccountMetrics(String userId, AccountMetricsQueryRequest request) {
        try {
            if (request.getAccountId() != null) {
                Long accountId = Long.parseLong(request.getAccountId());
                String cacheKey = quotaManager.generateMetricsCacheKey("account", request.getAccountId(), request.getDateRange());
                
                // 캐시에서 먼저 확인
                AccountMetricsQueryResponse cached = quotaManager.getFromMetricsCache(cacheKey, AccountMetricsQueryResponse.class);
                if (cached != null) {
                    log.info("Returning cached metrics for accountId: {}", accountId);
                    return List.of(cached);
                }
                
                // 할당량 체크
                if (!quotaManager.checkQuotaLimit(YouTubeApiQuotaManager.ApiPriority.REAL_TIME_QUERY)) {
                    log.warn("YouTube API quota limit reached. Returning empty response for accountId: {}", accountId);
                    return List.of();
                }
                
                // 특정 계정의 실시간 메트릭 조회
                SnsAccount account = snsAccountRepositoryPort.findById(accountId)
                    .orElseThrow(() -> new AnalyticsException("Account not found: " + accountId));
                
                // YouTube API로 실시간 메트릭 조회
                var statistics = youtubeApiService.getChannelStatistics(account.getSnsAccountId());
                
                if (statistics != null) {
                    
                    // 응답 형식 통일: DB 데이터와 동일한 형식으로 변환
                    AccountMetricsQueryResponse response = new AccountMetricsQueryResponse(
                        accountId,
                        account.getSnsAccountId(),
                        statistics.getSubscriberCount() != null ? statistics.getSubscriberCount().longValue() : 0L,
                        statistics.getViewCount() != null ? statistics.getViewCount().longValue() : 0L,
                        LocalDateTime.now()
                    );
                    
                    // 캐시에 저장
                    quotaManager.putToMetricsCache(cacheKey, response);
                    
                    return List.of(response);
                }
            }
            
            return List.of();
        } catch (AnalyticsException e) {
            log.error("Analytics error while getting current account metrics: {}", e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            log.error("Unexpected error while getting current account metrics", e);
            return List.of();
        }
    }
    
    private LocalDateTime getStartDate(String dateRange) {
        LocalDateTime now = LocalDateTime.now();
        return switch (dateRange) {
            case "today" -> now.toLocalDate().atStartOfDay();
            case "3days" -> now.minusDays(3);
            case "1week" -> now.minusWeeks(1);
            case "1month" -> now.minusMonths(1);
            case "6months" -> now.minusMonths(6);
            case "1year" -> now.minusYears(1);
            default -> now.minusWeeks(1); // 기본값: 1주일
        };
    }
    
    private PostMetricsQueryResponse toSnsPostMetricsQueryResponse(SnsPostMetric metric) {
        // SnsPost 엔티티에서 추가 정보 조회
        SnsPost post = snsPostRepositoryPort.findById(metric.getPostId()).orElse(null);
        
        return new PostMetricsQueryResponse(
            metric.getPostId(),
            post != null ? post.getSnsPostId() : "unknown",
            post != null ? post.getAccountId() : null,
            metric.getLikes() != null ? metric.getLikes().toString() : "0",
            metric.getDislikes(),
            metric.getComments(),
            metric.getShares(),
            metric.getViews(),
            metric.getCrawledAt()
        );
    }
    
    private AccountMetricsQueryResponse toSnsAccountMetricsQueryResponse(SnsAccountMetric metric) {
        // SnsAccount 엔티티에서 추가 정보 조회
        SnsAccount snsAccount = snsAccountRepositoryPort.findById(metric.getAccountId()).orElse(null);
        
        return new AccountMetricsQueryResponse(
            snsAccount != null ? snsAccount.getId() : null,
            snsAccount != null ? snsAccount.getSnsAccountId() : "unknown",
            metric.getFollowers(),
            metric.getViews(),
            metric.getCrawledAt()
        );
    }
    
    @Override
    public List<PostCommentsQueryResponse> getPostComments(String userId, PostCommentsQueryRequest request) {
        log.info("Getting post comments for userId: {}, dateRange: {}, postId: {}, page: {}, size: {}", 
                userId, request.getDateRange(), request.getPostId(), request.getPage(), request.getSize());
        
        // current인 경우 실시간 데이터 조회
        if ("current".equals(request.getDateRange())) {
            return getCurrentPostComments(userId, request);
        }
        
        LocalDateTime startDate = getStartDate(request.getDateRange());
        LocalDateTime endDate = LocalDateTime.now();
        
        List<SnsPostCommentMetric> comments;
        if (request.getPostId() != null) {
            // 특정 게시물의 댓글 조회 (페이지네이션 적용)
            comments = snsPostCommentMetricRepositoryPort.findByPostIdAndCrawledAtBetweenWithPagination(
                Long.parseLong(request.getPostId()), startDate, endDate, request.getPage(), request.getSize());
        } else {
            // 사용자의 모든 게시물 댓글 조회 (페이지네이션 적용)
            List<Long> accountIds = snsAccountRepositoryPort.findByUserId(Long.parseLong(userId))
                .stream()
                .map(account -> account.getId())
                .collect(Collectors.toList());
            
            List<Long> postIds = snsPostRepositoryPort.findByAccountId(accountIds.get(0))
                .stream()
                .map(post -> post.getId())
                .collect(Collectors.toList());
            
            comments = snsPostCommentMetricRepositoryPort.findByPostIdsAndCrawledAtBetweenWithPagination(
                postIds, startDate, endDate, request.getPage(), request.getSize());
        }
        
        return comments.stream()
            .map(this::toSnsPostCommentsQueryResponse)
            .collect(Collectors.toList());
    }
    
    private List<PostCommentsQueryResponse> getCurrentPostComments(String userId, PostCommentsQueryRequest request) {
        try {
            if (request.getPostId() != null) {
                Long postId = Long.parseLong(request.getPostId());
                String cacheKey = quotaManager.generateCommentsCacheKey(request.getPostId(), request.getDateRange(), request.getPage(), request.getSize());
                
                // 캐시에서 먼저 확인
                @SuppressWarnings("unchecked")
                List<PostCommentsQueryResponse> cached = quotaManager.getFromCommentsCache(cacheKey, List.class);
                if (cached != null) {
                    log.info("Returning cached comments for postId: {}, page: {}, size: {}", 
                        postId, request.getPage(), request.getSize());
                    return cached;
                }
                
                // 할당량 체크
                if (!quotaManager.checkQuotaLimit(YouTubeApiQuotaManager.ApiPriority.REAL_TIME_QUERY)) {
                    log.warn("YouTube API quota limit reached. Returning empty response for postId: {}", postId);
                    return List.of();
                }
                
                // 특정 게시물의 실시간 댓글 조회
                SnsPost post = snsPostRepositoryPort.findById(postId)
                    .orElseThrow(() -> new AnalyticsException("Post not found: " + postId));
                
                // YouTube API로 실시간 댓글 조회
                YouTube youtube = new YouTube.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    new com.google.api.client.json.gson.GsonFactory(),
                    null
                ).build();
                
                CommentThreadListResponse commentResponse = youtube.commentThreads()
                    .list(Arrays.asList("snippet"))
                    .setVideoId(post.getSnsPostId())
                    .setMaxResults((long) request.getSize())
                    .setOrder("time") // 최신순 정렬
                    .setKey(youtubeApiService.getApiKey())
                    .execute();
                
                // API 호출 카운트 증가
                quotaManager.incrementApiCall();
                
                if (commentResponse.getItems() != null) {
                    List<PostCommentsQueryResponse> responses = commentResponse.getItems().stream()
                        .map(commentThread -> {
                            var comment = commentThread.getSnippet().getTopLevelComment();
                            String commentId = commentThread.getSnippet().getTopLevelComment().getId();
                            String commentId2 = comment.getId();
                            
                            log.info("Comment ID comparison - commentThread: {}, comment: {}", commentId, commentId2);
                            
                            // 응답 형식 통일: DB 데이터와 동일한 형식으로 변환
                            return new PostCommentsQueryResponse(
                                commentId,
                                post.getSnsPostId(),
                                comment.getSnippet().getTextDisplay(),
                                LocalDateTime.now()
                            );
                        })
                        .collect(Collectors.toList());
                    
                    // 캐시에 저장
                    quotaManager.putToCommentsCache(cacheKey, responses);
                    
                    return responses;
                }
            }
            
            return List.of();
        } catch (AnalyticsException e) {
            log.error("Analytics error while getting current post comments: {}", e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            log.error("Unexpected error while getting current post comments", e);
            return List.of();
        }
    }
    
    private PostCommentsQueryResponse toSnsPostCommentsQueryResponse(SnsPostCommentMetric comment) {
        // SnsPost 엔티티에서 추가 정보 조회
        SnsPost post = snsPostRepositoryPort.findById(comment.getPostId()).orElse(null);
        
        return new PostCommentsQueryResponse(
            comment.getSnsCommentId(),
            post != null ? post.getSnsPostId() : "unknown",
            comment.getContent(),
            comment.getCrawledAt()
        );
    }
    

}
