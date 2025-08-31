package kt.aivle.analytics.adapter.out.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import kt.aivle.analytics.adapter.out.infrastructure.dto.AiReportRequest;
import kt.aivle.analytics.adapter.out.infrastructure.dto.AiReportResponse;
import kt.aivle.analytics.application.port.out.dto.AiAnalysisRequest;
import kt.aivle.analytics.application.port.out.dto.AiAnalysisResponse;
import kt.aivle.analytics.application.port.out.infrastructure.AiAnalysisPort;
import kt.aivle.analytics.application.port.out.repository.PostCommentKeywordRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.domain.model.SentimentType;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAnalysisAdapter implements AiAnalysisPort {
    
    private final RestTemplate restTemplate;
    private final PostCommentKeywordRepositoryPort keywordRepository;
    
    @Value("${ai.origin-url}")
    private String aiOriginUrl;
    
    // 처리 중인 AI 분석 작업을 추적하는 캐시
    private final Map<Long, CompletableFuture<AiReportResponse>> processingTasks = new ConcurrentHashMap<>();
    
    @Override
    public AiAnalysisResponse analyzeComments(List<SnsPostCommentMetric> comments, Long postId) {
        try {
            // 기존 키워드를 감정별로 그룹화하여 조회
            Map<SentimentType, List<String>> groupedKeywords = keywordRepository.findKeywordsByPostIdGroupedBySentiment(postId);
            
            List<String> positiveKeywords = groupedKeywords.getOrDefault(SentimentType.POSITIVE, List.of());
            List<String> negativeKeywords = groupedKeywords.getOrDefault(SentimentType.NEGATIVE, List.of());
            
            // 요청 데이터 구성
            List<AiAnalysisRequest.CommentData> commentDataList = comments.stream()
                .map(comment -> AiAnalysisRequest.CommentData.builder()
                    .id(comment.getId())
                    .created_at(comment.getCreatedAt().toString())
                    .author_id(comment.getAuthorId())
                    .content(comment.getContent())
                    .like_count(comment.getLikeCount().intValue())
                    .post_id(comment.getPostId())
                    .published_at(comment.getPublishedAt().toString())
                    .sns_comment_id(comment.getSnsCommentId())
                    .build())
                .collect(Collectors.toList());
            
            AiAnalysisRequest.Keywords keywords = AiAnalysisRequest.Keywords.builder()
                .positive(positiveKeywords)
                .negative(negativeKeywords)
                .build();
            
            AiAnalysisRequest request = AiAnalysisRequest.builder()
                .comments(commentDataList)
                .keywords(keywords)
                .build();
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AiAnalysisRequest> entity = new HttpEntity<>(request, headers);
            
            String analysisUrl = aiOriginUrl + "/api/comments/analyze";
            log.info("🚀 AI 서버 요청 시작 - URL: {}, 댓글 수: {}, 긍정 키워드: {}, 부정 키워드: {}", 
                analysisUrl, comments.size(), positiveKeywords.size(), negativeKeywords.size());
            
            // AI 분석 서버 호출
            log.info("📤 AI 서버로 요청 전송 중...");
            AiAnalysisResponse response = restTemplate.postForObject(analysisUrl, entity, AiAnalysisResponse.class);
            log.info("🚀 AI 서버 응답: {}", response);

            if (response.getIndividual_results() == null) {
                log.error("❌ AI 서버 응답의 individual_results가 null입니다");
                throw new BusinessException(AnalyticsErrorCode.AI_ANALYSIS_ERROR);
            }
            
            log.info("✅ AI 분석 완료 - 댓글 수: {}, 응답 상태: 성공", comments.size());
            return response;
            
        } catch (Exception e) {
            log.error("Failed to analyze comments with AI service: {}", e.getMessage());
            throw new BusinessException(AnalyticsErrorCode.AI_ANALYSIS_ERROR);
        }
    }
    
    @Override
    public AiReportResponse generateReport(AiReportRequest request, Long storeId) {
        Long postId = request.getMetrics().getPost_id();
        
        try {
            // 1. 이미 처리 중인지 확인
            CompletableFuture<AiReportResponse> existingTask = processingTasks.get(postId);
            if (existingTask != null && !existingTask.isDone()) {
                log.info("🔄 [AI] Already processing postId: {}, waiting for existing task", postId);
                try {
                    return existingTask.get(5, TimeUnit.MINUTES); // 기존 작업 완료 대기
                } catch (Exception e) {
                    log.warn("Existing task failed for postId: {}, starting new one", postId);
                    processingTasks.remove(postId);
                }
            }
            
            // 2. 새 작업 시작
            CompletableFuture<AiReportResponse> newTask = CompletableFuture.supplyAsync(() -> {
                try {
                    return callAiService(request, storeId);
                } finally {
                    processingTasks.remove(postId); // 완료 후 제거
                }
            });
            
            processingTasks.put(postId, newTask);
            
            log.info("🤖 [AI] Starting new analysis - postId: {}, title: {}", postId, request.getTitle());
            
            return newTask.get(5, TimeUnit.MINUTES);
            
        } catch (Exception e) {
            processingTasks.remove(postId);
            log.error("❌ [AI] Failed - postId: {}, error: {}", postId, e.getMessage());
            throw new BusinessException(AnalyticsErrorCode.AI_ANALYSIS_ERROR);
        }
    }
    
    private AiReportResponse callAiService(AiReportRequest request, Long storeId) {
        try {
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AiReportRequest> entity = new HttpEntity<>(request, headers);
            
            String reportUrl = aiOriginUrl + "/api/analysis/report";
            
            // AI 보고서 생성 서버 호출
            AiReportResponse response = restTemplate.postForObject(reportUrl, entity, AiReportResponse.class);
            
            if (response == null || response.getMarkdown_report() == null) {
                log.error("❌ [AI] Response is null or markdown_report is null");
                throw new BusinessException(AnalyticsErrorCode.AI_ANALYSIS_ERROR);
            }
            
            log.info("✅ [AI] Received response - postId: {}", request.getMetrics().getPost_id());
            return response;
            
        } catch (Exception e) {
            log.error("❌ [AI] Failed - postId: {}, error: {}", 
                    request.getMetrics().getPost_id(), e.getMessage());
            throw new BusinessException(AnalyticsErrorCode.AI_ANALYSIS_ERROR);
        }
    }
}
