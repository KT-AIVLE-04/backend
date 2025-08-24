package kt.aivle.analytics.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import kt.aivle.analytics.application.port.out.infrastructure.ValidationPort;
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
    private final ValidationPort validationPort;
    
    // ===== PUBLIC METHODS =====
    
    // 실시간 데이터 조회 메서드들
    @Override
    @Cacheable(value = "realtime-post-metrics", key = "'post-' + #userId + ',' + #snsType + ',' + #postId")
    public PostMetricsResponse getRealtimePostMetrics(String userId, String snsType, String postId) {
        SnsType snsTypeEnum = validationPort.validateAndParseSnsType(snsType);
        
        PostMetricsQueryRequest queryRequest;
        if (postId != null && !postId.trim().isEmpty()) {
            queryRequest = PostMetricsQueryRequest.forCurrentDate(postId);
        } else {
            queryRequest = PostMetricsQueryRequest.forLatestPostBySnsType(userId, snsTypeEnum);
        }
        
        return getRealtimePostMetricsInternal(userId, queryRequest);
    }
    
    @Override
    @Cacheable(value = "realtime-account-metrics", key = "'account-' + #userId + ',' + #snsType")
    public AccountMetricsResponse getRealtimeAccountMetrics(String userId, String snsType) {
        SnsType snsTypeEnum = validationPort.validateAndParseSnsType(snsType);
        
        AccountMetricsQueryRequest queryRequest = AccountMetricsQueryRequest.forCurrentDateAndSnsType(userId, snsTypeEnum);
        
        return getRealtimeAccountMetricsInternal(userId, queryRequest);
    }
    
    @Override
    @Cacheable(value = "realtime-comments", key = "'comments-' + #userId + ',' + #snsType + ',' + #postId + ',' + #page + ',' + #size")
    public List<PostCommentsResponse> getRealtimePostComments(String userId, String snsType, String postId, Integer page, Integer size) {
        SnsType snsTypeEnum = validationPort.validateAndParseSnsType(snsType);
        
        PostCommentsQueryRequest queryRequest;
        
        if (postId != null && !postId.trim().isEmpty()) {
            queryRequest = PostCommentsQueryRequest.forCurrentDate(postId, page, size);
        } else {
            queryRequest = PostCommentsQueryRequest.forLatestPostBySnsType(userId, snsTypeEnum, page, size);
        }
        
        return getRealtimePostCommentsInternal(userId, queryRequest);
    }
    
    // 히스토리 데이터 조회 메서드들
    @Override
    @Cacheable(value = "history-post-metrics", key = "'history-post-' + #userId + ',' + #dateStr + ',' + #snsType + ',' + #postId")
    public PostMetricsResponse getHistoricalPostMetrics(String userId, String dateStr, String snsType, String postId) {
        LocalDate date = validationPort.validateAndParseDate(dateStr);
        SnsType snsTypeEnum = validationPort.validateAndParseSnsType(snsType);
        
        PostMetricsQueryRequest queryRequest;
        if (postId != null && !postId.trim().isEmpty()) {
            queryRequest = PostMetricsQueryRequest.forDate(date, postId);
        } else {
            queryRequest = PostMetricsQueryRequest.forLatestPostBySnsType(date, userId, snsTypeEnum);
        }
        
        List<PostMetricsResponse> responses = getPostMetricsInternal(userId, queryRequest);
        
        // 특정 날짜에 여러 데이터가 있을 경우 시간이 늦은 것을 반환
        if (!responses.isEmpty()) {
            return responses.stream()
                .max((r1, r2) -> r1.getFetchedAt().compareTo(r2.getFetchedAt()))
                .orElse(null);
        }
        
        return null;
    }
    
    @Override
    @Cacheable(value = "history-account-metrics", key = "'history-account-' + #userId + ',' + #dateStr + ',' + #snsType")
    public AccountMetricsResponse getHistoricalAccountMetrics(String userId, String dateStr, String snsType) {
        LocalDate date = validationPort.validateAndParseDate(dateStr);
        SnsType snsTypeEnum = validationPort.validateAndParseSnsType(snsType);
        
        AccountMetricsQueryRequest queryRequest = AccountMetricsQueryRequest.forDateAndSnsType(date, userId, snsTypeEnum);
        
        return getAccountMetricsInternal(userId, queryRequest);
    }
    
    @Override
    @Cacheable(value = "history-comments", key = "'history-comments-' + #userId + ',' + #dateStr + ',' + #snsType + ',' + #postId + ',' + #page + ',' + #size")
    public List<PostCommentsResponse> getHistoricalPostComments(String userId, String dateStr, String snsType, String postId, Integer page, Integer size) {
        LocalDate date = validationPort.validateAndParseDate(dateStr);
        SnsType snsTypeEnum = validationPort.validateAndParseSnsType(snsType);
        
        PostCommentsQueryRequest queryRequest;
        if (postId != null && !postId.trim().isEmpty()) {
            queryRequest = PostCommentsQueryRequest.forDate(date, postId, page, size);
        } else {
            queryRequest = PostCommentsQueryRequest.forLatestPostBySnsType(userId, snsTypeEnum, page, size);
        }
        
        return getPostCommentsInternal(userId, queryRequest);
    }
    
    @Override
    @Cacheable(value = "history-emotion-analysis", key = "'history-emotion-' + #userId + ',' + #dateStr + ',' + #snsType + ',' + #postId")
    public EmotionAnalysisResponse getHistoricalEmotionAnalysis(String userId, String dateStr, String snsType, String postId) {
        validationPort.validatePostId(postId);
        
        LocalDate date = validationPort.validateAndParseDate(dateStr);
        SnsType snsTypeEnum = validationPort.validateAndParseSnsType(snsType);
        
        return getHistoricalEmotionAnalysisInternal(userId, postId, snsTypeEnum, date);
    }
    
    // ===== PRIVATE METHODS =====
    
    private List<PostMetricsResponse> getPostMetricsInternal(String userId, PostMetricsQueryRequest request) {
        log.info("Getting post metrics for userId: {}, date: {}, postId: {}, snsType: {}", 
                userId, request.getDate(), request.getPostId(), request.getSnsType());
        
        LocalDate targetDate = request.getEffectiveDate();
        
        List<Object[]> results;
        if (request.getPostId() != null) {
            // 특정 게시물의 메트릭 조회
            results = snsPostMetricRepositoryPort.findMetricsWithPostAndAccount(
                List.of(Long.parseLong(request.getPostId())), targetDate);
        } else {
            // SNS 타입과 사용자 ID로 최근 게시물만 조회
            Long latestPostId = getLatestPostIdBySnsType(Long.parseLong(request.getUserId()), request.getSnsType());
            results = snsPostMetricRepositoryPort.findMetricsWithPostAndAccount(
                List.of(latestPostId), targetDate);
        }
        
        return toSnsPostMetricsResponseFromJoin(results);
    }
    
    private AccountMetricsResponse getAccountMetricsInternal(String userId, AccountMetricsQueryRequest request) {
        log.info("Getting account metrics for userId: {}, date: {}, snsType: {}", 
                userId, request.getDate(), request.getSnsType());
        
        LocalDate targetDate = request.getEffectiveDate();
        
        // SNS 타입과 사용자 ID로 조회
        List<Long> accountIds = getAccountIdsByUserIdAndSnsType(Long.parseLong(request.getUserId()), request.getSnsType());
        
        List<Object[]> results = snsAccountMetricRepositoryPort.findMetricsWithAccount(accountIds, targetDate);
        
        List<AccountMetricsResponse> responses = toSnsAccountMetricsResponseFromJoin(results);
        
        // 특정 날짜에 여러 데이터가 있을 경우 시간이 늦은 것을 반환
        if (!responses.isEmpty()) {
            return responses.stream()
                .max((r1, r2) -> r1.getFetchedAt().compareTo(r2.getFetchedAt()))
                .orElse(null);
        }
        
        return null;
    }
    
    private List<PostCommentsResponse> getPostCommentsInternal(String userId, PostCommentsQueryRequest request) {
        log.info("Getting post comments for userId: {}, postId: {}, snsType: {}, page: {}, size: {}", 
                userId, request.getPostId(), request.getSnsType(), request.getPage(), request.getSize());
        
        Long targetPostId;
        if (request.getPostId() != null) {
            targetPostId = Long.parseLong(request.getPostId());
        } else {
            targetPostId = getLatestPostIdBySnsType(Long.parseLong(request.getUserId()), request.getSnsType());
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
    private PostMetricsResponse getRealtimePostMetricsInternal(String userId, PostMetricsQueryRequest request) {
        log.info("Getting realtime post metrics for userId: {}, postId: {}, snsType: {}", userId, request.getPostId(), request.getSnsType());
        
        Long targetPostId = getTargetPostIdForRealtime(userId, request);
        
        log.info("🔍 [CACHE MISS] 외부 API 호출 - realtime-post-metrics, targetPostId: {}", targetPostId);
        return externalApiPort.getRealtimePostMetrics(targetPostId);
    }
    

    
    private AccountMetricsResponse getRealtimeAccountMetricsInternal(String userId, AccountMetricsQueryRequest request) {
        log.info("Getting realtime account metrics for userId: {}, snsType: {}", userId, request.getSnsType());
        
        // SNS 타입과 사용자 ID로 조회
        List<Long> accountIds = getAccountIdsByUserIdAndSnsType(Long.parseLong(request.getUserId()), request.getSnsType());
        
        log.info("🔍 [CACHE MISS] 외부 API 호출 - realtime-account-metrics, userId: {}, snsType: {}", userId, request.getSnsType());
        
        List<AccountMetricsResponse> responses = accountIds.stream()
            .flatMap(accountId -> externalApiPort.getRealtimeAccountMetrics(accountId).stream())
            .collect(Collectors.toList());
        
        // SNS 타입이 지정된 경우 첫 번째 결과만 반환 (계정은 SNS 타입별로 하나씩만 존재)
        return responses.isEmpty() ? null : responses.get(0);
    }
    
    private List<PostCommentsResponse> getRealtimePostCommentsInternal(String userId, PostCommentsQueryRequest request) {
        log.info("Getting realtime post comments for userId: {}, postId: {}, snsType: {}", userId, request.getPostId(), request.getSnsType());
        
        Long targetPostId = getTargetPostIdForRealtime(userId, request);
        
        log.info("🔍 [CACHE MISS] 외부 API 호출 - realtime-comments, targetPostId: {}", targetPostId);
        
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
            return Long.parseLong(request.getPostId());
        } else {
            return getLatestPostIdBySnsType(Long.parseLong(request.getUserId()), request.getSnsType());
        }
    }
    
    /**
     * 실시간 조회를 위한 타겟 게시물 ID 조회 (댓글용)
     */
    private Long getTargetPostIdForRealtime(String userId, PostCommentsQueryRequest request) {
        if (request.getPostId() != null) {
            return Long.parseLong(request.getPostId());
        } else {
            return getLatestPostIdBySnsType(Long.parseLong(request.getUserId()), request.getSnsType());
        }
    }
    
    /**
     * 감정분석을 위한 타겟 게시물 ID 조회
     */
    private Long getTargetPostId(String userId, String postId, SnsType snsType) {
        if (postId != null && !postId.trim().isEmpty()) {
            return Long.parseLong(postId);
        } else {
            return getLatestPostIdBySnsType(Long.parseLong(userId), snsType);
        }
    }
    
    // 변환 메서드들
    
    /**
     * JOIN 쿼리 결과를 PostMetricsResponse로 변환
     */
    private List<PostMetricsResponse> toSnsPostMetricsResponseFromJoin(List<Object[]> results) {
        if (results.isEmpty()) {
            return List.of();
        }
        
        return results.stream()
            .map(result -> {
                SnsPostMetric metric = (SnsPostMetric) result[0];
                SnsPost post = (SnsPost) result[1];
                SnsAccount account = (SnsAccount) result[2];
                
                return PostMetricsResponse.builder()
                    .postId(metric.getPostId())
                    .accountId(post.getAccountId())
                    .likes(metric.getLikes())
                    .dislikes(metric.getDislikes())
                    .comments(metric.getComments())
                    .shares(metric.getShares())
                    .views(metric.getViews())
                    .fetchedAt(metric.getCreatedAt())
                    .snsType(account.getType())
                    .build();
            })
            .collect(Collectors.toList());
    }
    

    

    
    /**
     * JOIN 쿼리 결과를 AccountMetricsResponse로 변환
     */
    private List<AccountMetricsResponse> toSnsAccountMetricsResponseFromJoin(List<Object[]> results) {
        if (results.isEmpty()) {
            return List.of();
        }
        
        return results.stream()
            .map(result -> {
                SnsAccountMetric metric = (SnsAccountMetric) result[0];
                SnsAccount account = (SnsAccount) result[1];
                
                return AccountMetricsResponse.builder()
                    .accountId(metric.getAccountId())
                    .followers(metric.getFollowers())
                    .views(metric.getViews())
                    .fetchedAt(metric.getCreatedAt())
                    .snsType(account.getType())
                    .build();
            })
            .collect(Collectors.toList());
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
