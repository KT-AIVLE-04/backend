package kt.aivle.analytics.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimeAccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimePostMetricsResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.application.port.out.SnsAccountMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsPostCommentMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsPostMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsPostRepositoryPort;
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
    @Cacheable(value = "post-metrics", key = "#userId + '-' + #request.postId + '-' + #request.date")
    public List<PostMetricsQueryResponse> getPostMetrics(String userId, PostMetricsQueryRequest request) {
        log.info("Getting post metrics for userId: {}, date: {}, accountId: {}, postId: {}", 
                userId, request.getDate(), request.getAccountId(), request.getPostId());
        
        // 실시간 데이터 조회인 경우
        if (request.isCurrentDate()) {
            return getCurrentPostMetrics(userId, request);
        }
        
        LocalDateTime startDate = getStartDate(request.getEffectiveDate());
        LocalDateTime endDate = LocalDateTime.now();
        
        List<SnsPostMetric> metrics;
        if (request.getPostId() != null) {
            // 특정 게시물의 메트릭 조회
            metrics = snsPostMetricRepositoryPort.findByPostIdAndCreatedAtBetween(
                Long.parseLong(request.getPostId()), startDate, endDate);
        } else if (request.getAccountId() != null) {
            // 특정 계정의 모든 게시물 메트릭 조회
            List<SnsPost> posts = snsPostRepositoryPort.findByAccountId(Long.parseLong(request.getAccountId()));
            metrics = posts.stream()
                .flatMap(post -> snsPostMetricRepositoryPort.findByPostIdAndCreatedAtBetween(post.getId(), startDate, endDate).stream())
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
                        .flatMap(post -> snsPostMetricRepositoryPort.findByPostIdAndCreatedAtBetween(post.getId(), startDate, endDate).stream());
                })
                .collect(Collectors.toList());
        }
        
        return metrics.stream()
            .map(this::toSnsPostMetricsQueryResponse)
            .collect(Collectors.toList());
    }
    
    @Async
    public CompletableFuture<List<PostMetricsQueryResponse>> getPostMetricsAsync(String userId, PostMetricsQueryRequest request) {
        return CompletableFuture.completedFuture(getPostMetrics(userId, request));
    }
    
    @Override
    public List<AccountMetricsQueryResponse> getAccountMetrics(String userId, AccountMetricsQueryRequest request) {
        log.info("Getting account metrics for userId: {}, date: {}, accountId: {}", 
                userId, request.getDate(), request.getAccountId());
        
        // 실시간 데이터 조회인 경우
        if (request.isCurrentDate()) {
            return getCurrentAccountMetrics(userId, request);
        }
        
        LocalDateTime startDate = getStartDate(request.getEffectiveDate());
        LocalDateTime endDate = LocalDateTime.now();
        
        List<SnsAccountMetric> metrics;
        if (request.getAccountId() != null) {
            // 특정 계정의 메트릭 조회
            metrics = snsAccountMetricRepositoryPort.findByAccountIdAndCreatedAtBetween(
                Long.parseLong(request.getAccountId()), startDate, endDate);
        } else {
            // 사용자의 모든 계정 메트릭 조회
            List<Long> accountIds = snsAccountRepositoryPort.findByUserId(Long.parseLong(userId))
                .stream()
                .map(account -> account.getId())
                .collect(Collectors.toList());
            
            metrics = accountIds.stream()
                .flatMap(accountId -> snsAccountMetricRepositoryPort.findByAccountIdAndCreatedAtBetween(accountId, startDate, endDate).stream())
                .collect(Collectors.toList());
        }
        
        return metrics.stream()
            .map(this::toSnsAccountMetricsQueryResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<PostCommentsQueryResponse> getPostComments(String userId, PostCommentsQueryRequest request) {
        log.info("Getting post comments for userId: {}, date: {}, postId: {}, page: {}, size: {}", 
                userId, request.getDate(), request.getPostId(), request.getPage(), request.getSize());
        
        // 실시간 데이터 조회인 경우
        if (request.isCurrentDate()) {
            return getCurrentPostComments(userId, request);
        }
        
        LocalDateTime startDate = getStartDate(request.getEffectiveDate());
        LocalDateTime endDate = LocalDateTime.now();
        
        List<SnsPostCommentMetric> comments;
        if (request.getPostId() != null) {
            // 특정 게시물의 댓글 조회
            comments = snsPostCommentMetricRepositoryPort.findByPostIdAndCreatedAtBetween(
                Long.parseLong(request.getPostId()), startDate, endDate);
        } else {
            // 사용자의 모든 게시물 댓글 조회
            List<Long> accountIds = snsAccountRepositoryPort.findByUserId(Long.parseLong(userId))
                .stream()
                .map(account -> account.getId())
                .collect(Collectors.toList());
            
            comments = accountIds.stream()
                .flatMap(accountId -> {
                    List<SnsPost> posts = snsPostRepositoryPort.findByAccountId(accountId);
                    return posts.stream()
                        .flatMap(post -> snsPostCommentMetricRepositoryPort.findByPostIdAndCreatedAtBetween(post.getId(), startDate, endDate).stream());
                })
                .collect(Collectors.toList());
        }
        
        // 페이지네이션 적용
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), comments.size());
        
        return comments.subList(start, end).stream()
            .map(this::toSnsPostCommentsQueryResponse)
            .collect(Collectors.toList());
    }
    
    // 실시간 데이터 조회 메서드들
    @Override
    public List<RealtimePostMetricsResponse> getRealtimePostMetrics(String userId, PostMetricsQueryRequest request) {
        log.info("Getting realtime post metrics for userId: {}, postId: {}", userId, request.getPostId());
        
        if (request.getPostId() == null) {
            throw new AnalyticsException("PostId is required for realtime metrics");
        }
        
        return youtubeApiService.getRealtimePostMetrics(Long.parseLong(request.getPostId()));
    }
    
    @Override
    public List<RealtimeAccountMetricsResponse> getRealtimeAccountMetrics(String userId, AccountMetricsQueryRequest request) {
        log.info("Getting realtime account metrics for userId: {}, accountId: {}", userId, request.getAccountId());
        
        if (request.getAccountId() == null) {
            throw new AnalyticsException("AccountId is required for realtime metrics");
        }
        
        return youtubeApiService.getRealtimeAccountMetrics(Long.parseLong(request.getAccountId()));
    }
    
    @Override
    public List<PostCommentsQueryResponse> getRealtimePostComments(String userId, PostCommentsQueryRequest request) {
        log.info("Getting realtime post comments for userId: {}, postId: {}", userId, request.getPostId());
        
        if (request.getPostId() == null) {
            throw new AnalyticsException("PostId is required for realtime comments");
        }
        
        return youtubeApiService.getRealtimePostComments(Long.parseLong(request.getPostId()), request.getPage(), request.getSize());
    }
    
    // 헬퍼 메서드들
    private LocalDateTime getStartDate(LocalDate date) {
        return date.atStartOfDay();
    }
    
    private List<PostMetricsQueryResponse> getCurrentPostMetrics(String userId, PostMetricsQueryRequest request) {
        // 실시간 데이터 조회 로직을 PostMetricsQueryResponse로 변환
        return getRealtimePostMetrics(userId, request).stream()
            .map(this::convertToPostMetricsQueryResponse)
            .collect(Collectors.toList());
    }
    
    private List<AccountMetricsQueryResponse> getCurrentAccountMetrics(String userId, AccountMetricsQueryRequest request) {
        // 실시간 데이터 조회 로직
        return getRealtimeAccountMetrics(userId, request).stream()
            .map(this::convertToAccountMetricsQueryResponse)
            .collect(Collectors.toList());
    }
    
    private List<PostCommentsQueryResponse> getCurrentPostComments(String userId, PostCommentsQueryRequest request) {
        // 실시간 데이터 조회 로직
        return getRealtimePostComments(userId, request);
    }
    
    // 변환 메서드들
    private PostMetricsQueryResponse toSnsPostMetricsQueryResponse(SnsPostMetric metric) {
        return PostMetricsQueryResponse.builder()
            .postId(metric.getPostId())
            .likes(metric.getLikes() != null ? metric.getLikes().toString() : "0")
            .dislikes(metric.getDislikes())
            .comments(metric.getComments())
            .shares(metric.getShares())
            .views(metric.getViews())
            .crawledAt(metric.getCreatedAt())
            .build();
    }
    
    private AccountMetricsQueryResponse toSnsAccountMetricsQueryResponse(SnsAccountMetric metric) {
        return AccountMetricsQueryResponse.builder()
            .accountId(metric.getAccountId())
            .followers(metric.getFollowers())
            .views(metric.getViews())
            .crawledAt(metric.getCreatedAt())
            .build();
    }
    
    private PostCommentsQueryResponse toSnsPostCommentsQueryResponse(SnsPostCommentMetric comment) {
        return PostCommentsQueryResponse.builder()
            .commentId(comment.getSnsCommentId())
            .authorId(comment.getAuthorId())
            .text(comment.getContent())
            .likeCount(comment.getLikeCount())
            .publishedAt(comment.getPublishedAt())
            .crawledAt(comment.getCreatedAt())
            .build();
    }
    
    private AccountMetricsQueryResponse convertToAccountMetricsQueryResponse(RealtimeAccountMetricsResponse realtime) {
        return AccountMetricsQueryResponse.builder()
            .accountId(realtime.getAccountId())
            .followers(realtime.getFollowers())
            .views(realtime.getViews())
            .crawledAt(realtime.getFetchedAt())
            .build();
    }
    
    private PostMetricsQueryResponse convertToPostMetricsQueryResponse(RealtimePostMetricsResponse realtime) {
        return PostMetricsQueryResponse.builder()
            .postId(realtime.getPostId())
            .likes(realtime.getLikes())
            .dislikes(realtime.getDislikes())
            .comments(realtime.getComments())
            .shares(realtime.getShares())
            .views(realtime.getViews())
            .crawledAt(realtime.getFetchedAt())
            .build();
    }
}
