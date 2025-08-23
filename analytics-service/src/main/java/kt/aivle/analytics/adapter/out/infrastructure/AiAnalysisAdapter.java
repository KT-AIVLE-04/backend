package kt.aivle.analytics.adapter.out.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    
    @Value("${ai.analysis.url}")
    private String aiAnalysisUrl;
    
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
            
            log.info("🚀 AI 서버 요청 시작 - URL: {}, 댓글 수: {}, 긍정 키워드: {}, 부정 키워드: {}", 
                aiAnalysisUrl, comments.size(), positiveKeywords.size(), negativeKeywords.size());
            
            // AI 분석 서버 호출
            log.info("📤 AI 서버로 요청 전송 중...");
            AiAnalysisResponse response = restTemplate.postForObject(aiAnalysisUrl, entity, AiAnalysisResponse.class);

            if (response.getEmotionAnalysis() == null) {
                log.error("❌ AI 서버 응답의 emotionAnalysis가 null입니다");
                throw new BusinessException(AnalyticsErrorCode.AI_ANALYSIS_ERROR);
            }
            
            log.info("✅ AI 분석 완료 - 댓글 수: {}, 응답 상태: 성공", comments.size());
            return response;
            
        } catch (Exception e) {
            log.error("Failed to analyze comments with AI service: {}", e.getMessage());
            throw new BusinessException(AnalyticsErrorCode.AI_ANALYSIS_ERROR);
        }
    }
}
