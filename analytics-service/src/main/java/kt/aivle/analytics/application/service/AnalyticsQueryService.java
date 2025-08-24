package kt.aivle.analytics.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.EmotionAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;
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
import kt.aivle.analytics.domain.entity.SnsAccount;
import kt.aivle.analytics.domain.entity.SnsAccountMetric;
import kt.aivle.analytics.domain.entity.SnsPost;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.domain.entity.SnsPostMetric;
import kt.aivle.analytics.domain.model.SentimentType;
import kt.aivle.analytics.domain.model.SnsType;
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
    public List<PostMetricsResponse> getPostMetrics(String userId, PostMetricsQueryRequest request) {
        log.info("Getting post metrics for userId: {}, date: {}, postId: {}, snsType: {}", 
                userId, request.getDate(), request.getPostId(), request.getSnsType());
        
        LocalDate targetDate = request.getEffectiveDate();
        
        List<SnsPostMetric> metrics;
        if (request.getPostId() != null) {
            // 특정 게시물의 메트릭 조회
            metrics = snsPostMetricRepositoryPort.findByPostIdAndCreatedAtDate(
                Long.parseLong(request.getPostId()), targetDate);
        } else if (request.getSnsType() != null && request.getUserId() != null) {
            // SNS 타입과 사용자 ID로 최근 게시물만 조회
            Long latestPostId = getLatestPostIdBySnsType(Long.parseLong(request.getUserId()), request.getSnsType());
            metrics = snsPostMetricRepositoryPort.findByPostIdAndCreatedAtDate(latestPostId, targetDate);
        } else {
            // 사용자의 모든 계정 메트릭 조회
            List<Long> accountIds = getAccountIdsByUserId(Long.parseLong(userId));
            
            metrics = accountIds.stream()
                .flatMap(accountId -> {
                    List<SnsPost> posts = snsPostRepositoryPort.findByAccountId(accountId);
                    return posts.stream()
                        .flatMap(post -> snsPostMetricRepositoryPort.findByPostIdAndCreatedAtDate(post.getId(), targetDate).stream());
                })
                .collect(Collectors.toList());
        }
        
        // N+1 문제 해결: 중복 제거 후 배치 조회
        return toSnsPostMetricsResponseBatch(metrics);
    }

    
    @Override
    @Cacheable(value = "account-metrics", key = "'history-account-' + #userId + ',' + #request.snsType + ',' + #request.date")
    public AccountMetricsResponse getAccountMetrics(String userId, AccountMetricsQueryRequest request) {
        log.info("Getting account metrics for userId: {}, date: {}, snsType: {}", 
                userId, request.getDate(), request.getSnsType());
        
        LocalDate targetDate = request.getEffectiveDate();
        
        List<SnsAccountMetric> metrics;
        if (request.getSnsType() != null && request.getUserId() != null) {
            // SNS 타입과 사용자 ID로 조회
            List<Long> accountIds = getAccountIdsByUserIdAndSnsType(Long.parseLong(request.getUserId()), request.getSnsType());
            
            metrics = accountIds.stream()
                .flatMap(accountId -> snsAccountMetricRepositoryPort.findByAccountIdAndCreatedAtDate(accountId, targetDate).stream())
                .collect(Collectors.toList());
        } else {
            // 사용자의 모든 계정 메트릭 조회 (히스토리 API에서 snsType이 선택사항일 때)
            List<Long> accountIds = getAccountIdsByUserId(Long.parseLong(userId));
            
            metrics = accountIds.stream()
                .flatMap(accountId -> snsAccountMetricRepositoryPort.findByAccountIdAndCreatedAtDate(accountId, targetDate).stream())
                .collect(Collectors.toList());
        }
        
        List<AccountMetricsResponse> responses = metrics.stream()
            .map(this::toSnsAccountMetricsResponse)
            .collect(Collectors.toList());
        
        // SNS 타입이 지정된 경우 첫 번째 결과만 반환 (계정은 SNS 타입별로 하나씩만 존재)
        if (request.getSnsType() != null && !responses.isEmpty()) {
            return responses.get(0);
        }
        
        // SNS 타입이 지정되지 않은 경우 첫 번째 결과 반환 (기본값)
        return responses.isEmpty() ? null : responses.get(0);
    }
    
    @Override
    @Cacheable(value = "comments", key = "#request.postId + '-' + #request.snsType + '-' + #request.page + '-' + #request.size")
    public List<PostCommentsResponse> getPostComments(String userId, PostCommentsQueryRequest request) {
        log.info("Getting post comments for userId: {}, postId: {}, snsType: {}, page: {}, size: {}", 
                userId, request.getPostId(), request.getSnsType(), request.getPage(), request.getSize());
        
        Long targetPostId;
        if (request.getPostId() != null) {
            targetPostId = Long.parseLong(request.getPostId());
        } else if (request.getSnsType() != null && request.getUserId() != null) {
            targetPostId = getLatestPostIdBySnsType(Long.parseLong(request.getUserId()), request.getSnsType());
        } else {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        // 특정 게시물의 댓글 조회 (날짜 조건 없음)
        List<SnsPostCommentMetric> comments = snsPostCommentMetricRepositoryPort.findByPostId(targetPostId);
        
        // 페이지네이션 적용
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), comments.size());
        
        if (start >= comments.size()) {
            return List.of(); // 빈 목록 반환
        }
        
        return comments.subList(start, end).stream()
            .map(this::toSnsPostCommentsResponse)
            .collect(Collectors.toList());
    }
    

    
    @Override
    @Cacheable(value = "emotion-analysis", key = "#userId + '-' + #postId + '-' + #snsType + '-' + #date")
    public EmotionAnalysisResponse getHistoricalEmotionAnalysis(String userId, String postId, SnsType snsType, LocalDate date) {
        return getHistoricalEmotionAnalysisInternal(userId, postId, snsType, date);
    }
    
    /**
     * 내부 히스토리 감정분석 로직 (캐시 적용)
     */
    private EmotionAnalysisResponse getHistoricalEmotionAnalysisInternal(String userId, String postId, SnsType snsType, LocalDate date) {
        log.info("Getting historical emotion analysis for userId: {}, postId: {}, snsType: {}, date: {}", userId, postId, snsType, date);
        
        Long targetPostId = getTargetPostId(userId, postId, snsType);
        
        // 게시물 존재 여부 확인
        validatePostExists(targetPostId);
        
        // 특정 날짜의 감정분석 결과 조회
        List<SnsPostCommentMetric> commentMetrics = snsPostCommentMetricRepositoryPort.findByPostIdAndCreatedAtDate(targetPostId, date);
        
        return buildEmotionAnalysisResponse(targetPostId, commentMetrics);
    }
    
    // 실시간 데이터 조회 메서드들
    @Override
    @Cacheable(value = "realtime-post-metrics", key = "#userId + '-' + #request.postId + '-' + #request.snsType")
    public List<PostMetricsResponse> getRealtimePostMetrics(String userId, PostMetricsQueryRequest request) {
        log.info("Getting realtime post metrics for userId: {}, postId: {}, snsType: {}", userId, request.getPostId(), request.getSnsType());
        
        Long targetPostId = getTargetPostIdForRealtime(userId, request);
        return externalApiPort.getRealtimePostMetrics(targetPostId);
    }
    
    @Override
    @Cacheable(value = "realtime-account-metrics", key = "'account-' + #userId + ',' + #request.snsType")
    public AccountMetricsResponse getRealtimeAccountMetrics(String userId, AccountMetricsQueryRequest request) {
        log.info("Getting realtime account metrics for userId: {}, snsType: {}", userId, request.getSnsType());
        
        if (request.getSnsType() != null && request.getUserId() != null) {
            // SNS 타입과 사용자 ID로 조회
            List<Long> accountIds = getAccountIdsByUserIdAndSnsType(Long.parseLong(request.getUserId()), request.getSnsType());
            
            log.info("🔍 [CACHE MISS] 외부 API 호출 - realtime-account-metrics, userId: {}, snsType: {}", userId, request.getSnsType());
            
            List<AccountMetricsResponse> responses = accountIds.stream()
                .flatMap(accountId -> externalApiPort.getRealtimeAccountMetrics(accountId).stream())
                .collect(Collectors.toList());
            
            // SNS 타입이 지정된 경우 첫 번째 결과만 반환 (계정은 SNS 타입별로 하나씩만 존재)
            return responses.isEmpty() ? null : responses.get(0);
        } else {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
    }
    
    @Override
    @Cacheable(value = "comments", key = "#targetPostId")
    public List<PostCommentsResponse> getRealtimePostComments(String userId, PostCommentsQueryRequest request) {
        log.info("Getting realtime post comments for userId: {}, postId: {}, snsType: {}", userId, request.getPostId(), request.getSnsType());
        
        Long targetPostId = getTargetPostIdForRealtime(userId, request);
        
        // 로컬 postId로 DB에서 snsPostId 조회
        SnsPost post = snsPostRepositoryPort.findById(targetPostId)
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND));
        
        // 외부 API에서 댓글 조회
        List<PostCommentsResponse> comments = externalApiPort.getVideoComments(post.getSnsPostId());
        log.info("Retrieved comments from external API for postId: {}, comment count: {}", targetPostId, comments.size());
        
        return comments;
    }
    
    // 헬퍼 메서드들
    
    /**
     * 사용자 ID로 계정 ID 목록 조회
     */
    private List<Long> getAccountIdsByUserId(Long userId) {
        return snsAccountRepositoryPort.findByUserId(userId)
            .stream()
            .map(SnsAccount::getId)
            .toList();
    }
    
    /**
     * 사용자 ID와 SNS 타입으로 계정 ID 목록 조회
     */
    private List<Long> getAccountIdsByUserIdAndSnsType(Long userId, SnsType snsType) {
        return snsAccountRepositoryPort.findByUserIdAndSnsType(userId, snsType)
            .stream()
            .map(SnsAccount::getId)
            .toList();
    }
    
    /**
     * SNS 타입으로 최근 게시물 ID 조회
     */
    private Long getLatestPostIdBySnsType(Long userId, SnsType snsType) {
        List<Long> accountIds = getAccountIdsByUserIdAndSnsType(userId, snsType);
        
        if (accountIds.isEmpty()) {
            throw new BusinessException(AnalyticsErrorCode.ACCOUNT_NOT_FOUND);
        }
        
        // 각 계정의 최근 게시물 조회 (최적화)
        Optional<SnsPost> latestPost = accountIds.stream()
            .map(snsPostRepositoryPort::findLatestByAccountId)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .max((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()));
        
        if (latestPost.isEmpty()) {
            throw new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND);
        }
        
        return latestPost.get().getId();
    }
    
    /**
     * 게시물 존재 여부 확인
     */
    private void validatePostExists(Long postId) {
        snsPostRepositoryPort.findById(postId)
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND));
    }
    
    /**
     * 감정분석 응답 생성
     */
    private EmotionAnalysisResponse buildEmotionAnalysisResponse(Long postId, List<SnsPostCommentMetric> commentMetrics) {
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
    
    /**
     * 실시간 조회를 위한 타겟 게시물 ID 조회
     */
    private Long getTargetPostIdForRealtime(String userId, PostMetricsQueryRequest request) {
        if (request.getPostId() != null) {
            // 특정 게시물 조회
            return Long.parseLong(request.getPostId());
        } else if (request.getSnsType() != null && request.getUserId() != null) {
            // 해당 SNS의 최근 게시물 조회
            return getLatestPostIdBySnsType(Long.parseLong(request.getUserId()), request.getSnsType());
        } else {
            throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
        }
    }
    
    /**
     * 실시간 조회를 위한 타겟 게시물 ID 조회 (댓글용)
     */
    private Long getTargetPostIdForRealtime(String userId, PostCommentsQueryRequest request) {
        if (request.getPostId() != null) {
            // 특정 게시물 조회
            return Long.parseLong(request.getPostId());
        } else if (request.getSnsType() != null && request.getUserId() != null) {
            // 해당 SNS의 최근 게시물 조회
            return getLatestPostIdBySnsType(Long.parseLong(request.getUserId()), request.getSnsType());
        } else {
            throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
        }
    }
    
    /**
     * 감정분석을 위한 타겟 게시물 ID 조회
     */
    private Long getTargetPostId(String userId, String postId, SnsType snsType) {
        if (postId != null) {
            return Long.parseLong(postId);
        } else {
            // 해당 SNS의 최근 게시물 조회
            return getLatestPostIdBySnsType(Long.parseLong(userId), snsType);
        }
    }
    
    // 변환 메서드들
    
    /**
     * N+1 문제 해결을 위한 배치 변환 메서드
     * postId가 같으면 accountId도 같으므로 중복 조회를 피함
     */
    private List<PostMetricsResponse> toSnsPostMetricsResponseBatch(List<SnsPostMetric> metrics) {
        if (metrics.isEmpty()) {
            return List.of();
        }
        
        // 1단계: 필요한 postId들 수집 (중복 제거)
        Set<Long> postIds = metrics.stream()
            .map(SnsPostMetric::getPostId)
            .collect(Collectors.toSet());
        
        // 2단계: Post 정보를 배치로 조회
        Map<Long, SnsPost> posts = snsPostRepositoryPort.findAllById(postIds)
            .stream()
            .collect(Collectors.toMap(SnsPost::getId, post -> post));
        
        // 3단계: 필요한 accountId들 수집 (중복 제거)
        Set<Long> accountIds = posts.values().stream()
            .map(SnsPost::getAccountId)
            .collect(Collectors.toSet());
        
        // 4단계: Account 정보를 배치로 조회
        Map<Long, SnsAccount> accounts = snsAccountRepositoryPort.findAllById(accountIds)
            .stream()
            .collect(Collectors.toMap(SnsAccount::getId, account -> account));
        
        // 5단계: 메모리에서 매핑하여 Response 생성
        return metrics.stream()
            .map(metric -> {
                SnsPost post = posts.get(metric.getPostId());
                SnsAccount account = post != null ? accounts.get(post.getAccountId()) : null;
                SnsType snsType = account != null ? account.getType() : null;
                
                return PostMetricsResponse.builder()
                    .postId(metric.getPostId())
                    .accountId(post != null ? post.getAccountId() : null)
                    .likes(metric.getLikes())
                    .dislikes(metric.getDislikes())
                    .comments(metric.getComments())
                    .shares(metric.getShares())
                    .views(metric.getViews())
                    .fetchedAt(metric.getCreatedAt())
                    .snsType(snsType)
                    .build();
            })
            .collect(Collectors.toList());
    }
    

    
    private AccountMetricsResponse toSnsAccountMetricsResponse(SnsAccountMetric metric) {
        // Account 정보를 통해 SNS 타입 조회
        SnsAccount account = snsAccountRepositoryPort.findById(metric.getAccountId())
            .orElse(null);
        SnsType snsType = account != null ? account.getType() : null;
        
        return AccountMetricsResponse.builder()
            .accountId(metric.getAccountId())
            .followers(metric.getFollowers())
            .views(metric.getViews())
            .fetchedAt(metric.getCreatedAt())
            .snsType(snsType)
            .build();
    }
    
    private PostCommentsResponse toSnsPostCommentsResponse(SnsPostCommentMetric comment) {
        return PostCommentsResponse.builder()
            .commentId(comment.getSnsCommentId())  // SNS ID 사용
            .authorId(comment.getAuthorId())
            .text(comment.getContent())
            .likeCount(comment.getLikeCount())
            .publishedAt(comment.getPublishedAt())
            .build();
    }
}
