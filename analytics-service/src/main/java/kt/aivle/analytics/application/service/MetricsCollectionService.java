package kt.aivle.analytics.application.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsPageResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
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
    
    // AI 분석용 전용 스레드 풀 (최대 5개 동시 실행)
    private final ExecutorService aiAnalysisExecutor = Executors.newFixedThreadPool(5);
    
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
                            log.error("Failed to collect {} for {}: {}", itemType, idExtractor.apply(item), e.getMessage());
                        }
                    } catch (Exception e) {
                        failedIds.add(idExtractor.apply(item));
                        log.error("Failed to collect {} for {}: {}", itemType, idExtractor.apply(item), e.getMessage());
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
            log.error("Failed to collect {}: {}", itemType, e.getMessage());
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
        }
    }
    
    @Override
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void collectAccountMetricsByAccountId(Long accountId) {
        log.info("Collecting account metrics for accountId: {}", accountId);

        SnsAccount snsAccount = snsAccountRepositoryPort.findById(accountId)
            .orElseThrow(() -> new BusinessException(AnalyticsErrorCode.ACCOUNT_NOT_FOUND));

        if (snsAccount.getType() != SnsType.youtube) {
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
            log.error("Failed to collect account metrics for accountId: {}: {}", accountId, e.getMessage());
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
                Long likeCount = statistics.likeCount();
                Long dislikeCount = 0L; // YouTube API v3에서는 dislike count를 제공하지 않음
                Long commentCount = statistics.commentCount();
                Long viewCount = statistics.viewCount();
                
                // 데이터 유효성 검증
                MetricsData metricsData = new MetricsData(null, viewCount, likeCount, commentCount, "post", postId);
                validationPort.validateMetrics(metricsData);
                
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
                    .shares(null)
                    .views(viewCount)
                    .build();
                
                snsPostMetricRepositoryPort.save(postMetric);
                log.info("Saved post metrics for postId: {}, likes: {}, dislikes: {}, comments: {}, views: {}", 
                    postId, likeCount, dislikeCount, commentCount, viewCount);
            }
            
        } catch (Exception e) {
            log.error("Failed to collect post metrics for postId: {}: {}", postId, e.getMessage());
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
            log.error("Failed to collect comments for postId: {}: {}", postId, e.getMessage());
            throw new BusinessException(AnalyticsErrorCode.INTERNAL_ERROR);
        }
    }
    
    // API 호출 메서드 (트랜잭션 외부)
    private List<SnsPostCommentMetric> fetchCommentsFromAPI(SnsPost post, Long postId) throws IOException {
        List<SnsPostCommentMetric> newComments = new ArrayList<>();
        
        log.info("🔍 댓글 수집 시작 - postId: {}, snsPostId: {}", postId, post.getSnsPostId());
        
        // 페이지네이션을 통한 댓글 수집
        String pageToken = null;
        int pageCount = 0;
        int totalCommentsFetched = 0;
        
        do {
            pageCount++;
            log.info("📄 댓글 수집 중 - postId: {}, 페이지: {}, pageToken: {}", 
                postId, pageCount, pageToken != null ? "있음" : "없음");
            
            // ExternalApiPort를 통해 댓글 조회 (페이지네이션 지원)
            PostCommentsPageResponse pageResponse = externalApiPort.getVideoCommentsWithPagination(post.getSnsPostId(), pageToken, 100);
            List<PostCommentsResponse> pageComments = pageResponse.getData();
            
            if (pageComments.isEmpty()) {
                log.info("📄 빈 페이지 - postId: {}, 페이지: {}", postId, pageCount);
                break;
            }
            
            totalCommentsFetched += pageComments.size();
            log.info("📄 페이지 댓글 수집 완료 - postId: {}, 페이지: {}, 댓글 수: {}, 누적: {}", 
                postId, pageCount, pageComments.size(), totalCommentsFetched);
            
            // 페이지의 댓글들을 처리
            for (PostCommentsResponse comment : pageComments) {
                try {
                    // 이미 DB에 있는 댓글인지 확인
                    if (snsPostCommentMetricRepositoryPort.findBySnsCommentId(comment.getSnsCommentId()).isPresent()) {
                        log.info("🛑 기존 댓글 발견 - postId: {}, 페이지: {}, commentId: {}, 수집 중단. 총 수집: {}", 
                            postId, pageCount, comment.getSnsCommentId(), newComments.size());
                        return newComments; // 이미 있는 댓글을 만나면 수집 중단
                    }
                    
                    // 새로운 댓글 처리
                    String content = comment.getText();
                    // 긴 댓글은 1000자로 제한 (DB TEXT 타입이지만 안전하게)
                    if (content != null && content.length() > 1000) {
                        content = content.substring(0, 1000);
                        log.debug("댓글 내용 잘림 - commentId: {}, 길이: 1000자로 제한", comment.getSnsCommentId());
                    }
                    
                    SnsPostCommentMetric commentMetric = SnsPostCommentMetric.builder()
                        .snsCommentId(comment.getSnsCommentId())
                        .postId(post.getId())
                        .authorId(comment.getSnsAuthorId())  // null 가능
                        .content(content)
                        .likeCount(comment.getLikeCount())
                        .publishedAt(comment.getPublishedAt())
                        .build();
                    
                    newComments.add(commentMetric);
                    
                } catch (Exception e) {
                    log.error("댓글 처리 실패 - postId: {}, commentId: {}: {}", postId, comment.getSnsCommentId(), e.getMessage());
                }
            }
            
            // 다음 페이지 토큰 가져오기
            pageToken = pageResponse.getNextPageToken();
            
        } while (pageToken != null);
        
        log.info("✅ 댓글 수집 완료 - postId: {}, 총 페이지: {}, 총 조회: {}, 새 댓글: {}", 
            postId, pageCount, totalCommentsFetched, newComments.size());
        
        return newComments;
    }
    
    // DB 저장 메서드 (개별 저장으로 변경하여 일부 실패해도 계속 진행)
    private void saveCommentsToDatabase(List<SnsPostCommentMetric> newComments, Long postId) {
        log.info("💾 DB 저장 시작 - {}개의 새 댓글을 postId: {}에 저장", newComments.size(), postId);
        
        int savedCount = 0;
        for (SnsPostCommentMetric commentMetric : newComments) {
            try {
                // 개별 댓글을 직접 저장 (트랜잭션은 Repository 레벨에서 처리)
                SnsPostCommentMetric savedComment = snsPostCommentMetricRepositoryPort.save(commentMetric);
                if (savedComment != null && savedComment.getId() != null) {
                    savedCount++;
                    log.debug("Saved new comment for postId: {}, commentId: {}", postId, commentMetric.getSnsCommentId());
                } else {
                    log.warn("Failed to save comment - saved entity is null or has no ID for commentId: {}", commentMetric.getSnsCommentId());
                }
            } catch (Exception e) {
                log.error("Failed to save comment for commentId: {}: {}", commentMetric.getSnsCommentId(), e.getMessage());
                // 개별 댓글 저장 실패는 다른 댓글 저장에 영향을 주지 않음
            }
        }
        
        log.info("💾 DB 저장 완료 - postId: {}, 성공: {}/{}", postId, savedCount, newComments.size());
        
        // 새로운 댓글이 있으면 감정분석을 비동기로 수행
        if (savedCount > 0) {
            // DB에서 실제 저장된 댓글들을 조회 (ID 포함)
            List<SnsPostCommentMetric> savedComments = getSavedCommentsWithIds(newComments, postId);
            
            if (!savedComments.isEmpty()) {
                // 비동기로 감정분석 수행 (응답을 기다리지 않음)
                performEmotionAnalysisAsync(postId, savedComments);
                log.info("🧠 비동기 감정분석 시작 - postId: {}, 댓글 수: {}", postId, savedComments.size());
            }
        }
    }
    

    
    /**
     * DB에서 실제 저장된 댓글들을 조회 (ID 포함)
     */
    private List<SnsPostCommentMetric> getSavedCommentsWithIds(List<SnsPostCommentMetric> newComments, Long postId) {
        List<SnsPostCommentMetric> savedComments = new ArrayList<>();
        
        for (SnsPostCommentMetric comment : newComments) {
            try {
                // SNS 댓글 ID로 DB에서 조회하여 실제 DB ID를 가져옴
                var savedComment = snsPostCommentMetricRepositoryPort.findBySnsCommentId(comment.getSnsCommentId());
                savedComment.ifPresent(savedComments::add);
            } catch (Exception e) {
                log.warn("Failed to find saved comment for snsCommentId: {}", comment.getSnsCommentId(), e);
            }
        }
        
        log.info("Found {} saved comments with IDs for postId: {}", savedComments.size(), postId);
        return savedComments;
    }
    
    /**
     * 비동기로 감정분석 수행
     */
    @Async
    public void performEmotionAnalysisAsync(Long postId, List<SnsPostCommentMetric> comments) {
        try {
            log.info("Starting async emotion analysis for postId: {} with {} comments", postId, comments.size());
            
            // AI 분석을 별도 스레드에서 실행하여 응답을 기다리지 않음
            CompletableFuture.runAsync(() -> {
                try {
                    emotionAnalysisService.analyzeAndSaveEmotions(postId, comments);
                    log.info("Completed async emotion analysis for postId: {}", postId);
                } catch (Exception e) {
                    log.error("Failed to perform emotion analysis for postId: {}: {}", postId, e.getMessage());
                }
            }, aiAnalysisExecutor); // 커스텀 스레드 풀 사용

        } catch (Exception e) {
            log.error("Failed to start async emotion analysis for postId: {}: {}", postId, e.getMessage());
        }
    }
}

