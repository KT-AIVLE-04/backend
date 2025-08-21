package kt.aivle.analytics.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import kt.aivle.analytics.adapter.in.web.dto.AiAnalysisRequest;
import kt.aivle.analytics.adapter.in.web.dto.AiAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.application.port.out.PostCommentKeywordRepositoryPort;
import kt.aivle.analytics.domain.model.SentimentType;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.analytics.exception.AnalyticsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {
    
    private final RestTemplate restTemplate;
    private final PostCommentKeywordRepositoryPort keywordRepository;
    
    @Value("${ai.analysis.url:http://localhost:8081/analyze}")
    private String aiAnalysisUrl;
    
    /**
     * AI 분석 서버에 댓글 데이터를 전송하여 감정분석을 수행합니다.
     */
    public AiAnalysisResponse analyzeComments(List<PostCommentsQueryResponse> comments, Long postId) {
        try {
            // 기존 키워드를 감정별로 그룹화하여 조회
            Map<SentimentType, List<String>> groupedKeywords = keywordRepository.findKeywordsByPostIdGroupedBySentiment(postId);
            
            List<String> positiveKeywords = groupedKeywords.getOrDefault(SentimentType.POSITIVE, List.of());
            List<String> negativeKeywords = groupedKeywords.getOrDefault(SentimentType.NEGATIVE, List.of());
            
            // 요청 데이터 구성
            List<AiAnalysisRequest.CommentData> commentDataList = comments.stream()
                .map(comment -> AiAnalysisRequest.CommentData.builder()
                    .id(comment.getCommentId())
                    .result(comment.getText())  // text 필드 사용
                    .build())
                .collect(Collectors.toList());
            
            AiAnalysisRequest.Keywords keywords = AiAnalysisRequest.Keywords.builder()
                .positive(positiveKeywords)
                .negative(negativeKeywords)
                .build();
            
            AiAnalysisRequest request = AiAnalysisRequest.builder()
                .data(commentDataList)
                .keyword(keywords)
                .build();
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AiAnalysisRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Sending AI analysis request for {} comments with {} positive and {} negative existing keywords", 
                comments.size(), positiveKeywords.size(), negativeKeywords.size());
            
            // AI 분석 서버 호출
            AiAnalysisResponse response = restTemplate.postForObject(aiAnalysisUrl, entity, AiAnalysisResponse.class);
            
            if (response == null) {
                throw new AnalyticsException(AnalyticsErrorCode.AI_ANALYSIS_ERROR, "AI analysis response is null");
            }
            
            log.info("AI analysis completed successfully for {} comments", comments.size());
            return response;
            
        } catch (Exception e) {
            log.error("Failed to analyze comments with AI service", e);
            throw new AnalyticsException(AnalyticsErrorCode.AI_ANALYSIS_ERROR, "Failed to analyze comments", e);
        }
    }
}
