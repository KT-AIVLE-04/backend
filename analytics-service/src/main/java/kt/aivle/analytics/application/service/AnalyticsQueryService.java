package kt.aivle.analytics.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.event.dto.PostInfoResponseMessage;
import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.EmotionAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.ReportResponse;
import kt.aivle.analytics.adapter.in.websocket.dto.WebSocketResponseMessage;
import kt.aivle.analytics.adapter.out.infrastructure.dto.AiReportRequest;
import kt.aivle.analytics.adapter.out.infrastructure.dto.AiReportResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.application.port.out.SnsServicePort;
import kt.aivle.analytics.application.port.out.infrastructure.AiAnalysisPort;
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
    private final SnsPostRepositoryPort snsPostRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final PostCommentKeywordRepositoryPort postCommentKeywordRepository;
    private final ExternalApiPort externalApiPort;
    private final ValidationPort validationPort;
    private final AiAnalysisPort aiAnalysisPort;
    private final SnsServicePort snsServicePort;
    private final CacheManager cacheManager;
    
    // ===== PUBLIC METHODS =====
    
    // 실시간 데이터 조회 메서드들
    @Override
    @Cacheable(value = "realtime-post-metrics", key = "'post-' + #userId + ',' + #accountId + ',' + #postId")
    public PostMetricsResponse getRealtimePostMetrics(Long userId, Long accountId, Long postId) {
        validationPort.validateAccountId(accountId);
        
        PostMetricsQueryRequest queryRequest;
        if (postId != null) {
            queryRequest = PostMetricsQueryRequest.forCurrentDate(postId, accountId);
        } else {
            queryRequest = PostMetricsQueryRequest.forLatestPostByAccountId(accountId);
        }
        
        return getRealtimePostMetricsInternal(userId, queryRequest);
    }
    
    @Override
    @Cacheable(value = "realtime-account-metrics", key = "'account-' + #userId + ',' + #accountId")
    public AccountMetricsResponse getRealtimeAccountMetrics(Long userId, Long accountId) {
        validationPort.validateAccountId(accountId);
        
        AccountMetricsQueryRequest queryRequest = AccountMetricsQueryRequest.forCurrentDateAndAccountId(accountId);
        
        return getRealtimeAccountMetricsInternal(userId, queryRequest);
    }
    
    @Override
    @Cacheable(value = "realtime-comments", key = "'comments-' + #userId + ',' + #accountId + ',' + #postId + ',' + #page + ',' + #size")
    public List<PostCommentsResponse> getRealtimePostComments(Long userId, Long accountId, Long postId, Integer page, Integer size) {
        validationPort.validateAccountId(accountId);
        
        PostCommentsQueryRequest queryRequest;
        
        if (postId != null) {
            queryRequest = PostCommentsQueryRequest.forCurrentDate(postId, page, size, accountId);
        } else {
            queryRequest = PostCommentsQueryRequest.forLatestPostByAccountId(accountId, page, size);
        }
        
        return getRealtimePostCommentsInternal(userId, queryRequest);
    }
    
    // 히스토리 데이터 조회 메서드들
    @Override
    @Cacheable(value = "history-post-metrics", key = "'history-post-' + #userId + ',' + #dateStr + ',' + #accountId + ',' + #postId")
    public PostMetricsResponse getHistoricalPostMetrics(Long userId, String dateStr, Long accountId, Long postId) {
        LocalDate date = validationPort.validateAndParseDate(dateStr);
        validationPort.validateAccountId(accountId);
        
        PostMetricsQueryRequest queryRequest;
        if (postId != null) {
            queryRequest = PostMetricsQueryRequest.forDate(date, postId, accountId);
        } else {
            queryRequest = PostMetricsQueryRequest.forLatestPostByAccountId(date, accountId);
        }
        
        List<PostMetricsResponse> responses = getPostMetricsInternal(userId, queryRequest);
        
        // 특정 날짜에 여러 데이터가 있을 경우 시간이 늦은 것을 반환
        if (!responses.isEmpty()) {
            return responses.stream()
                .max((r1, r2) -> r1.getFetchedAt().compareTo(r2.getFetchedAt()))
                .orElse(PostMetricsResponse.builder()
                    .postId(postId)
                    .accountId(accountId)
                    .fetchedAt(LocalDateTime.now())
                    .build());
        }
        
        // 데이터가 없으면 기본값을 가진 객체 반환
        return PostMetricsResponse.builder()
            .postId(postId)
            .accountId(accountId)
            .fetchedAt(LocalDateTime.now())
            .build();
    }
    
    @Override
    @Cacheable(value = "history-account-metrics", key = "'history-account-' + #userId + ',' + #dateStr + ',' + #accountId")
    public AccountMetricsResponse getHistoricalAccountMetrics(Long userId, String dateStr, Long accountId) {
        LocalDate date = validationPort.validateAndParseDate(dateStr);
        validationPort.validateAccountId(accountId);
        
        AccountMetricsQueryRequest queryRequest = AccountMetricsQueryRequest.forDateAndAccountId(date, accountId);
        
        return getAccountMetricsInternal(userId, queryRequest);
    }
    
    @Override
    @Cacheable(value = "history-comments", key = "'history-comments-' + #userId + ',' + #dateStr + ',' + #accountId + ',' + #postId + ',' + #page + ',' + #size")
    public List<PostCommentsResponse> getHistoricalPostComments(Long userId, String dateStr, Long accountId, Long postId, Integer page, Integer size) {
        LocalDate date = validationPort.validateAndParseDate(dateStr);
        validationPort.validateAccountId(accountId);
        
        PostCommentsQueryRequest queryRequest;
        if (postId != null) {
            queryRequest = PostCommentsQueryRequest.forDate(date, postId, page, size, accountId);
        } else {
            queryRequest = PostCommentsQueryRequest.forLatestPostByAccountId(accountId, page, size);
        }
        
        return getHistoricalPostCommentsInternal(userId, queryRequest, date);
    }
    
    @Override
    @Cacheable(value = "history-emotion-analysis", key = "'history-emotion-' + #userId + ',' + #dateStr + ',' + #accountId + ',' + #postId")
    public EmotionAnalysisResponse getHistoricalEmotionAnalysis(Long userId, String dateStr, Long accountId, Long postId) {
        LocalDate date = validationPort.validateAndParseDate(dateStr);
        validationPort.validateAccountId(accountId);
        
        // postId가 null이거나 비어있으면 validation 건너뛰기
        if (postId != null) {
            validationPort.validatePostId(postId);
        }
        
        return getHistoricalEmotionAnalysisInternal(userId, postId, accountId, date);
    }
    
    // AI 보고서 생성 (REST API용)
    @Override
    @Cacheable(value = "report", key = "#postId + '_' + #userId + '_' + #accountId + '_' + #storeId")
    public ReportResponse generateReport(Long userId, Long accountId, Long postId, Long storeId) {
        log.info("service generateReport - postId: {}", postId);
        
        // WebSocket용 단계별 메서드들을 사용하여 코드 중복 제거
        PostInfoResponseMessage postInfo = getPostInfo(userId, accountId, postId, storeId);
        SnsPostMetric postMetric = getPostMetrics(userId, accountId, postId);
        
        return generateAiReport(userId, accountId, postId, storeId, postInfo, postMetric);
    }
    
    // 통합된 비동기 AI 보고서 생성 (WebSocket용) - 캐시 확인 포함
    @Override
    public CompletableFuture<WebSocketResponseMessage<ReportResponse>> generateReportAsync(Long accountId, Long postId, Long storeId) {
        log.info("[WebSocket] 비동기 AI 보고서 생성 시작 - postId: {}", postId);
        
        // accountId로 userId 조회
        Long userId = getUserIdByAccountId(accountId);
        log.info("[WebSocket] accountId {}로 userId {} 조회", accountId, userId);
        
        // 1. 캐시 확인
        return CompletableFuture.supplyAsync(() -> {
            log.info("[WebSocket] 캐시 확인 중 - postId: {}", postId);
            try {
                return getCachedReport(userId, accountId, postId, storeId);
            } catch (Exception e) {
                log.warn("[WebSocket] 캐시 확인 중 에러 발생, 계속 진행 - postId: {}, error: {}", postId, e.getMessage());
                return null; // 에러 시 null 반환하여 계속 진행
            }
        })
        .thenCompose(cachedReport -> {
            if (cachedReport != null) {
                // 캐시된 보고서가 있으면 즉시 완료
                log.info("[WebSocket] 캐시된 보고서 발견 - date: {}", cachedReport);
                return CompletableFuture.completedFuture(cachedReport);
            }
            
            // 2. 캐시가 없으면 단계별로 처리
            return CompletableFuture.supplyAsync(() -> {
                log.info("[WebSocket] 1단계: SNS 서비스에서 post 정보 가져오기 - postId: {}", postId);
                return getPostInfo(userId, accountId, postId, storeId);
            })
            .thenCompose(postInfo -> 
                CompletableFuture.supplyAsync(() -> {
                    log.info("[WebSocket] 2단계: 게시물 메트릭 조회 - postId: {}", postId);
                    return getPostMetrics(userId, accountId, postId);
                })
                .thenCompose(postMetrics -> 
                    CompletableFuture.supplyAsync(() -> {
                        log.info("[WebSocket] 3단계: AI 보고서 생성 - postId: {}", postId);
                        return generateAiReport(userId, accountId, postId, storeId, postInfo, postMetrics);
                    })
                )
            )
            .thenApply(reportResponse -> {
                // 새로 생성된 보고서를 캐시에 저장 (cachedReport가 null일 때만)
                try {
                    String cacheKey = postId + "_" + userId + "_" + accountId + "_" + storeId;
                    cacheManager.getCache("report").put(cacheKey, reportResponse);
                    log.info("[WebSocket] 새로 생성된 보고서를 캐시에 저장 - postId: {}, cacheKey: {}", postId, cacheKey);
                } catch (Exception e) {
                    log.warn("[WebSocket] 캐시 저장 중 에러 발생 - postId: {}, error: {}", postId, e.getMessage());
                }
                
                return reportResponse;
            });
        })
        .thenApply(reportResponse -> {
            log.info("[WebSocket] 최종 결과 반환 - data: {}", reportResponse);

            
            // 최종 결과를 WebSocketResponseMessage로 변환
            return WebSocketResponseMessage.complete(reportResponse, "AI 분석 보고서가 완성되었습니다!");
        });
    }
    
    // ===== PRIVATE METHODS =====
    
    private List<PostMetricsResponse> getPostMetricsInternal(Long userId, PostMetricsQueryRequest request) {
        log.info("Getting post metrics for userId: {}, date: {}, postId: {}, accountId: {}", 
                userId, request.getDate(), request.getPostId(), request.getAccountId());
        
        LocalDate targetDate = request.getEffectiveDate();
        
        List<Object[]> results;
        if (request.getPostId() != null) {
            // 특정 게시물의 메트릭 조회
            Long postId = request.getPostId();
            // postId가 제공된 경우 계정 ID 검증
            validatePostAccountId(postId, request.getAccountId());
            results = snsPostMetricRepositoryPort.findMetricsWithPostAndAccount(
                List.of(postId), targetDate);
        } else {
            // 계정 ID로 최근 게시물만 조회
            Long latestPostId = getLatestPostIdByAccountId(request.getAccountId());
            results = snsPostMetricRepositoryPort.findMetricsWithPostAndAccount(
                List.of(latestPostId), targetDate);
        }
        
        return toSnsPostMetricsResponseFromJoin(results);
    }
    
    private AccountMetricsResponse getAccountMetricsInternal(Long userId, AccountMetricsQueryRequest request) {
        log.info("Getting account metrics for userId: {}, date: {}, accountId: {}", 
                userId, request.getDate(), request.getAccountId());
        
        LocalDate targetDate = request.getEffectiveDate();
        
        // 계정 ID로 조회
        List<Object[]> results = snsAccountMetricRepositoryPort.findMetricsWithAccount(List.of(request.getAccountId()), targetDate);
        
        List<AccountMetricsResponse> responses = toSnsAccountMetricsResponseFromJoin(results);
        
        // 특정 날짜에 여러 데이터가 있을 경우 시간이 늦은 것을 반환
        if (!responses.isEmpty()) {
            return responses.stream()
                .max((r1, r2) -> r1.getFetchedAt().compareTo(r2.getFetchedAt()))
                .orElse(AccountMetricsResponse.builder()
                    .accountId(request.getAccountId())
                    .fetchedAt(LocalDateTime.now())
                    .build());
        }
        
        // 데이터가 없으면 기본값을 가진 객체 반환
        return AccountMetricsResponse.builder()
            .accountId(request.getAccountId())
            .fetchedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * 히스토리 댓글 조회 내부 로직 (날짜 기준 필터링)
     */
    private List<PostCommentsResponse> getHistoricalPostCommentsInternal(Long userId, PostCommentsQueryRequest request, LocalDate date) {
        log.info("Getting historical post comments for userId: {}, postId: {}, accountId: {}, date: {}, page: {}, size: {}", 
                userId, request.getPostId(), request.getAccountId(), date, request.getPage(), request.getSize());
        
        Long targetPostId;
        if (request.getPostId() != null) {
            targetPostId = request.getPostId();
            // postId가 제공된 경우 계정 ID 검증
            validatePostAccountId(targetPostId, request.getAccountId());
        } else {
            targetPostId = getLatestPostIdByAccountId(request.getAccountId());
        }
        
        // 날짜 기준으로 publishedAt 이전의 댓글을 최신순으로 페이지네이션하여 조회
        List<SnsPostCommentMetric> comments = snsPostCommentMetricRepositoryPort.findByPostIdAndPublishedAtBeforeWithPagination(
            targetPostId, date, request.getPage(), request.getSize());
        
        log.info("Retrieved historical comments from DB for postId: {}, date: {}, page: {}, size: {}, result count: {}", 
            targetPostId, date, request.getPage(), request.getSize(), comments.size());
        
        return comments.stream()
            .map(this::toSnsPostCommentsResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 내부 히스토리 감정분석 로직 (캐시 적용)
     */
    private EmotionAnalysisResponse getHistoricalEmotionAnalysisInternal(Long userId, Long postId, Long accountId, LocalDate date) {
        log.info("Getting historical emotion analysis for userId: {}, postId: {}, accountId: {}, date: {}", userId, postId, accountId, date);
        
        Long targetPostId;
        if (postId != null) {
            targetPostId = postId;
            // postId가 제공된 경우 계정 ID 검증
            validatePostAccountId(targetPostId, accountId);
        } else {
            // postId가 없으면 최근 게시물 사용
            targetPostId = getLatestPostIdByAccountId(accountId);
            log.info("No postId provided, using latest post for accountId: {}, latestPostId: {}", accountId, targetPostId);
        }
        
        // 게시물 존재 여부 확인
        validatePostExists(targetPostId);
        
        //감정분석 결과 조회
        List<SnsPostCommentMetric> commentMetrics = snsPostCommentMetricRepositoryPort.findByPostId(targetPostId);
        log.info("Found {} comments for postId: {} on date: {}", commentMetrics.size(), targetPostId, date);
        
        return buildEmotionAnalysisResponse(targetPostId, commentMetrics);
    }
    
    // 실시간 데이터 조회 메서드들
    private PostMetricsResponse getRealtimePostMetricsInternal(Long userId, PostMetricsQueryRequest request) {
        log.info("Getting realtime post metrics for userId: {}, postId: {}, accountId: {}", userId, request.getPostId(), request.getAccountId());
        
        Long targetPostId = getTargetPostIdForRealtime(userId, request);
        
        // postId가 제공된 경우 계정 ID 검증
        if (request.getPostId() != null) {
            validatePostAccountId(targetPostId, request.getAccountId());
        }
        
        log.info("🔍 [CACHE MISS] 외부 API 호출 - realtime-post-metrics, targetPostId: {}", targetPostId);
        return externalApiPort.getRealtimePostMetrics(targetPostId);
    }
    

    
    private AccountMetricsResponse getRealtimeAccountMetricsInternal(Long userId, AccountMetricsQueryRequest request) {
        log.info("Getting realtime account metrics for userId: {}, accountId: {}", userId, request.getAccountId());
        
        log.info("🔍 [CACHE MISS] 외부 API 호출 - realtime-account-metrics, userId: {}, accountId: {}", userId, request.getAccountId());
        
        List<AccountMetricsResponse> responses = externalApiPort.getRealtimeAccountMetrics(request.getAccountId());
        
        return responses.isEmpty() ? 
            AccountMetricsResponse.builder()
                .accountId(request.getAccountId())
                .fetchedAt(LocalDateTime.now())
                .build() : 
            responses.get(0);
    }
    
    private List<PostCommentsResponse> getRealtimePostCommentsInternal(Long userId, PostCommentsQueryRequest request) {
        log.info("Getting realtime post comments for userId: {}, postId: {}, accountId: {}", userId, request.getPostId(), request.getAccountId());
        
        Long targetPostId = getTargetPostIdForRealtime(userId, request);
        
        // postId가 제공된 경우 계정 ID 검증
        if (request.getPostId() != null) {
            validatePostAccountId(targetPostId, request.getAccountId());
        }
        
        log.info("🔍 [CACHE MISS] 외부 API 호출 - realtime-comments, targetPostId: {}", targetPostId);
        
        // 로컬 postId로 DB에서 snsPostId 조회
        SnsPost post = snsPostRepositoryPort.findById(targetPostId)
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND));
        
        // 외부 API에서 댓글 조회 (개수 제한 적용)
    
        List<PostCommentsResponse> comments = externalApiPort.getVideoCommentsWithLimit(post.getSnsPostId(), request.getSize());
        log.info("Retrieved comments from external API for postId: {}, comment count: {}", targetPostId, comments.size());
        
        // 페이지네이션 적용
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), comments.size());
        
        if (start >= comments.size()) {
            return List.of(); // 빈 목록 반환
        }
        
        List<PostCommentsResponse> paginatedComments = new ArrayList<>(comments.subList(start, end));
        log.info("Applied pagination for postId: {}, page: {}, size: {}, result count: {}", 
            targetPostId, request.getPage(), request.getSize(), paginatedComments.size());
        
        return paginatedComments;
    }
    
    // 헬퍼 메서드들
    

    

    

    
    /**
     * 계정 ID로 최근 게시물 ID 조회
     */
    private Long getLatestPostIdByAccountId(Long accountId) {
        Optional<SnsPost> latestPost = snsPostRepositoryPort.findLatestByAccountId(accountId);
        
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
     * 계정 ID로 사용자 ID 조회
     */
    private Long getUserIdByAccountId(Long accountId) {
        return snsAccountRepositoryPort.findUserIdById(accountId)
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.ACCOUNT_NOT_FOUND));
    }
    
    /**
     * 게시물의 계정 ID 검증
     */
    private void validatePostAccountId(Long postId, Long accountId) {
        SnsPost post = snsPostRepositoryPort.findById(postId)
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND));
        
        log.info("🔍 Post Account ID 검증: postId={}, expected accountId={}, actual accountId={}", 
            postId, accountId, post.getAccountId());
        
        if (!post.getAccountId().equals(accountId)) {
            log.warn("Post accountId mismatch: postId={}, expected accountId={}, actual accountId={}", 
                postId, accountId, post.getAccountId());
            throw new BusinessException(AnalyticsErrorCode.INVALID_ACCOUNT_ID);
        }
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
    private Long getTargetPostIdForRealtime(Long userId, PostMetricsQueryRequest request) {
        if (request.getPostId() != null) {
            return request.getPostId();
        } else {
            return getLatestPostIdByAccountId(request.getAccountId());
        }
    }
    
    /**
     * 실시간 조회를 위한 타겟 게시물 ID 조회 (댓글용)
     */
    private Long getTargetPostIdForRealtime(Long userId, PostCommentsQueryRequest request) {
        if (request.getPostId() != null) {
            return request.getPostId();
        } else {
            return getLatestPostIdByAccountId(request.getAccountId());
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
            .snsCommentId(comment.getSnsCommentId())  // SNS ID 사용
            .snsAuthorId(comment.getAuthorId())
            .text(comment.getContent())
            .likeCount(comment.getLikeCount())
            .publishedAt(comment.getPublishedAt())
            .build();
    }
    
    // ===== AI 보고서 단계별 생성 메서드들 (private) =====
    
    private PostInfoResponseMessage getPostInfo(Long userId, Long accountId, Long postId, Long storeId) {
        log.info("[WebSocket] 1단계: SNS 서비스에서 post 정보 가져오기 - postId: {}", postId);
        
        // 1단계에서만 계정 ID 검증
        validationPort.validateAccountId(accountId);
        
        try {
            var postInfoFuture = snsServicePort.getPostInfo(postId, userId, accountId, storeId);
            return postInfoFuture.get(); // CompletableFuture를 동기적으로 처리
        } catch (Exception e) {
            log.error("[WebSocket] SNS 서비스에서 post 정보를 가져오는 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(AnalyticsErrorCode.EXTERNAL_API_ERROR);
        }
    }
    
    private SnsPostMetric getPostMetrics(Long userId, Long accountId, Long postId) {
        log.info("[WebSocket] 2단계: 게시물 메트릭 조회 - postId: {}", postId);
        
        SnsPostMetric postMetric = snsPostMetricRepositoryPort.findLatestByPostId(postId)
            .orElseThrow(() -> {
                log.warn("[WebSocket] 게시물 메트릭을 찾을 수 없습니다 - Post ID: {}", postId);
                return new BusinessException(AnalyticsErrorCode.POST_NOT_FOUND);
            });
        
        log.info("[WebSocket] 게시물 메트릭 조회 성공 - Post ID: {}, Views: {}, Likes: {}, Comments: {}", 
            postId, postMetric.getViews(), postMetric.getLikes(), postMetric.getComments());
        
        return postMetric;
    }
    
    private ReportResponse generateAiReport(Long userId, Long accountId, Long postId, Long storeId, 
        PostInfoResponseMessage postInfo, 
        SnsPostMetric postMetric) {
        
        log.info("[WebSocket] 4단계: AI 보고서 생성 - postId: {}", postId);
        
        // 감정 분석 데이터 조회
        Map<SentimentType, List<String>> groupedKeywords = postCommentKeywordRepository.findKeywordsByPostIdGroupedBySentiment(postId);
        List<SnsPostCommentMetric> comments = snsPostCommentMetricRepositoryPort.findByPostId(postId);
        
        // AI 보고서 요청 데이터 구성
        AiReportRequest.Metrics metricsData = AiReportRequest.Metrics.builder()
            .postId(postId)
            .viewCount(postMetric.getViews())
            .likeCount(postMetric.getLikes())
            .commentCount(postMetric.getComments())
            .build();
        
        AiReportRequest.EmotionData emotionDataRequest = AiReportRequest.EmotionData.builder()
            .positiveCount((long) comments.stream().filter(c -> c.getSentiment() == SentimentType.POSITIVE).count())
            .negativeCount((long) comments.stream().filter(c -> c.getSentiment() == SentimentType.NEGATIVE).count())
            .neutralCount((long) comments.stream().filter(c -> c.getSentiment() == SentimentType.NEUTRAL).count())
            .positiveKeywords(groupedKeywords.getOrDefault(SentimentType.POSITIVE, List.of()))
            .negativeKeywords(groupedKeywords.getOrDefault(SentimentType.NEGATIVE, List.of()))
            .neutralKeywords(groupedKeywords.getOrDefault(SentimentType.NEUTRAL, List.of()))
            .build();
        
        AiReportRequest request = AiReportRequest.builder()
            .metrics(metricsData)
            .emotionData(emotionDataRequest)
            .title(postInfo.getTitle())
            .description(postInfo.getDescription())
            .url(postInfo.getUrl())
            .tags(postInfo.getTags())
            .publishAt(postInfo.getPublishAt() != null ? postInfo.getPublishAt().toString() : null)
            .build();
        
        // AI 서버에 보고서 생성 요청
        AiReportResponse aiResponse = aiAnalysisPort.generateReport(request, storeId);
        
        // 최종 ReportResponse 생성 (클라이언트용)
        return ReportResponse.builder()
            .postId(postId)
            .markdownReport(aiResponse.getMarkdownReport())
            .title(postInfo.getTitle())
            .description(postInfo.getDescription())
            .url(postInfo.getUrl())
            .publishAt(postInfo.getPublishAt() != null ? postInfo.getPublishAt().toString() : null)
            .build();
    }
    
    private ReportResponse getCachedReport(Long userId, Long accountId, Long postId, Long storeId) {
        log.info("[WebSocket] 캐시된 보고서 확인 - postId: {}", postId);
        
        // 캐시에서 보고서 조회 시도
        String cacheKey = postId + "_" + userId + "_" + accountId + "_" + storeId;
        ReportResponse cachedResponse = cacheManager.getCache("report").get(cacheKey, ReportResponse.class);
        
        if (cachedResponse != null) {
            log.info("[WebSocket] 캐시된 보고서 발견 - postId: {}", postId);
            return cachedResponse;
        }
        
        log.info("[WebSocket] 캐시된 보고서 없음 - postId: {}", postId);
        return null;
    }
}
