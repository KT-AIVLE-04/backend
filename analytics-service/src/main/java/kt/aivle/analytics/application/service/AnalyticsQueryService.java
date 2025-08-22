package kt.aivle.analytics.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.EmotionAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimeAccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimePostMetricsResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.application.port.out.infrastructure.ExternalApiPort;
import kt.aivle.analytics.application.port.out.repository.PostCommentKeywordRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsAccountMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsAccountRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsPostCommentMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsPostMetricRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsPostRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsAccountMetric;
import kt.aivle.analytics.domain.entity.SnsPost;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.domain.entity.SnsPostMetric;
import kt.aivle.analytics.domain.model.SentimentType;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.common.exception.BusinessException;
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
    private final PostCommentKeywordRepositoryPort postCommentKeywordRepository;
    private final ExternalApiPort externalApiPort;
    
    @Override
    @Cacheable(value = "post-metrics", key = "#userId + '-' + #request.postId + '-' + T(java.time.format.DateTimeFormatter).ISO_LOCAL_DATE.format(#request.getEffectiveDate())")
    public List<PostMetricsQueryResponse> getPostMetrics(String userId, PostMetricsQueryRequest request) {
        log.info("Getting post metrics for userId: {}, date: {}, accountId: {}, postId: {}", 
                userId, request.getDate(), request.getAccountId(), request.getPostId());
        
        LocalDate targetDate = request.getEffectiveDate();
        
        List<SnsPostMetric> metrics;
        if (request.getPostId() != null) {
            // 특정 게시물의 메트릭 조회
            metrics = snsPostMetricRepositoryPort.findByPostIdAndCreatedAtDate(
                Long.parseLong(request.getPostId()), targetDate);
        } else if (request.getAccountId() != null) {
            // 특정 계정의 모든 게시물 메트릭 조회
            List<SnsPost> posts = snsPostRepositoryPort.findByAccountId(Long.parseLong(request.getAccountId()));
            metrics = posts.stream()
                .flatMap(post -> snsPostMetricRepositoryPort.findByPostIdAndCreatedAtDate(post.getId(), targetDate).stream())
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
                        .flatMap(post -> snsPostMetricRepositoryPort.findByPostIdAndCreatedAtDate(post.getId(), targetDate).stream());
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
    @Cacheable(value = "account-metrics", key = "#userId + '-' + #request.accountId + '-' + T(java.time.format.DateTimeFormatter).ISO_LOCAL_DATE.format(#request.getEffectiveDate())")
    public List<AccountMetricsQueryResponse> getAccountMetrics(String userId, AccountMetricsQueryRequest request) {
        log.info("Getting account metrics for userId: {}, date: {}, accountId: {}", 
                userId, request.getDate(), request.getAccountId());
        
        LocalDate targetDate = request.getEffectiveDate();
        
        List<SnsAccountMetric> metrics;
        if (request.getAccountId() != null) {
            // 특정 계정의 메트릭 조회
            metrics = snsAccountMetricRepositoryPort.findByAccountIdAndCreatedAtDate(
                Long.parseLong(request.getAccountId()), targetDate);
        } else {
            // 사용자의 모든 계정 메트릭 조회
            List<Long> accountIds = snsAccountRepositoryPort.findByUserId(Long.parseLong(userId))
                .stream()
                .map(account -> account.getId())
                .collect(Collectors.toList());
            
            metrics = accountIds.stream()
                .flatMap(accountId -> snsAccountMetricRepositoryPort.findByAccountIdAndCreatedAtDate(accountId, targetDate).stream())
                .collect(Collectors.toList());
        }
        
        return metrics.stream()
            .map(this::toSnsAccountMetricsQueryResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "comments", key = "#request.postId + '-' + #request.page + '-' + #request.size + '-' + T(java.time.format.DateTimeFormatter).ISO_LOCAL_DATE.format(#request.getEffectiveDate())")
    public List<PostCommentsQueryResponse> getPostComments(String userId, PostCommentsQueryRequest request) {
        log.info("Getting post comments for userId: {}, date: {}, postId: {}, page: {}, size: {}", 
                userId, request.getDate(), request.getPostId(), request.getPage(), request.getSize());
        
        LocalDate targetDate = request.getEffectiveDate();
        
        List<SnsPostCommentMetric> comments;
        if (request.getPostId() != null) {
            // 특정 게시물의 댓글 조회
            comments = snsPostCommentMetricRepositoryPort.findByPostIdAndCreatedAtDate(
                Long.parseLong(request.getPostId()), targetDate);
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
                        .flatMap(post -> snsPostCommentMetricRepositoryPort.findByPostIdAndCreatedAtDate(post.getId(), targetDate).stream());
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
    
    @Override
    public EmotionAnalysisResponse getEmotionAnalysis(String userId, Long postId) {
        log.info("Getting emotion analysis for userId: {}, postId: {}", userId, postId);
        
        // 게시물 존재 여부 확인
        snsPostRepositoryPort.findById(postId)
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND));
        
        // 감정분석 결과 조회
        List<SnsPostCommentMetric> commentMetrics = snsPostCommentMetricRepositoryPort.findByPostId(postId);
        
        // 감정별 개수 계산 (null 제외)
        long positiveCount = commentMetrics.stream()
            .filter(metric -> metric.getSentiment() != null && SentimentType.POSITIVE.equals(metric.getSentiment()))
            .count();
        
        long neutralCount = commentMetrics.stream()
            .filter(metric -> metric.getSentiment() != null && SentimentType.NEUTRAL.equals(metric.getSentiment()))
            .count();
        
        long negativeCount = commentMetrics.stream()
            .filter(metric -> metric.getSentiment() != null && SentimentType.NEGATIVE.equals(metric.getSentiment()))
            .count();
        
        long totalCount = commentMetrics.size();
        
        // 키워드를 감정별로 그룹화하여 조회
        Map<SentimentType, List<String>> groupedKeywords = postCommentKeywordRepository.findKeywordsByPostIdGroupedBySentiment(postId);
        
        List<String> positiveKeywords = groupedKeywords.getOrDefault(SentimentType.POSITIVE, List.of());
        List<String> negativeKeywords = groupedKeywords.getOrDefault(SentimentType.NEGATIVE, List.of());
        
        Map<String, List<String>> keywords = Map.of(
            "positive", positiveKeywords,
            "negative", negativeKeywords
        );
        
        EmotionAnalysisResponse.EmotionSummary summary = EmotionAnalysisResponse.EmotionSummary.builder()
            .positiveCount(positiveCount)
            .neutralCount(neutralCount)
            .negativeCount(negativeCount)
            .totalCount(totalCount)
            .build();
        
        return EmotionAnalysisResponse.builder()
            .postId(postId)
            .emotionSummary(summary)
            .keywords(keywords)
            .build();
    }
    
    // 실시간 데이터 조회 메서드들
    @Override
    public List<RealtimePostMetricsResponse> getRealtimePostMetrics(String userId, PostMetricsQueryRequest request) {
        log.info("Getting realtime post metrics for userId: {}, postId: {}", userId, request.getPostId());
        
        if (request.getPostId() == null) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
        }
        
        return externalApiPort.getRealtimePostMetrics(Long.parseLong(request.getPostId()));
    }
    
    @Override
    public List<RealtimeAccountMetricsResponse> getRealtimeAccountMetrics(String userId, AccountMetricsQueryRequest request) {
        log.info("Getting realtime account metrics for userId: {}, accountId: {}", userId, request.getAccountId());
        
        if (request.getAccountId() == null) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_ACCOUNT_ID);
        }
        
        return externalApiPort.getRealtimeAccountMetrics(Long.parseLong(request.getAccountId()));
    }
    
    @Override
    public List<PostCommentsQueryResponse> getRealtimePostComments(String userId, PostCommentsQueryRequest request) {
        log.info("Getting realtime post comments for userId: {}, postId: {}", userId, request.getPostId());
        
        if (request.getPostId() == null) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
        }
        
        // 로컬 postId로 DB에서 snsPostId 조회
        SnsPost post = snsPostRepositoryPort.findById(Long.parseLong(request.getPostId()))
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND));
        
        return externalApiPort.getVideoComments(post.getSnsPostId());
    }
    
    // 헬퍼 메서드들
    
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
            .commentId(comment.getSnsCommentId())  // SNS ID 사용
            .authorId(comment.getAuthorId())
            .text(comment.getContent())
            .likeCount(comment.getLikeCount())
            .publishedAt(comment.getPublishedAt())
            .build();
    }
}
