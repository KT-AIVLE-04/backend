package kt.aivle.analytics.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import kt.aivle.analytics.adapter.in.web.dto.AiAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.application.port.out.PostCommentKeywordRepositoryPort;
import kt.aivle.analytics.application.port.out.SnsPostCommentMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.PostCommentKeyword;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.domain.model.SentimentType;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.analytics.exception.AnalyticsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionAnalysisService {
    
    private final SnsPostCommentMetricRepositoryPort commentMetricRepository;
    private final PostCommentKeywordRepositoryPort keywordRepository;
    private final AiAnalysisService aiAnalysisService;
    
    /**
     * 댓글 감정분석을 수행하고 결과를 저장합니다.
     */
    public void analyzeAndSaveEmotions(Long postId, List<PostCommentsQueryResponse> comments) {
        try {
            // AI 분석 수행 (기존 키워드는 AiAnalysisService 내부에서 조회)
            AiAnalysisResponse aiResponse = aiAnalysisService.analyzeComments(comments, postId);
            
            // 감정분석 결과 저장
            saveCommentMetrics(postId, comments, aiResponse.getEmotionAnalysis().getIndividualResults());
            
            // 키워드 저장
            saveKeywords(postId, aiResponse.getKeywords());
            
            log.info("Emotion analysis completed and saved for postId: {}", postId);
            
        } catch (Exception e) {
            log.error("Failed to analyze and save emotions for postId: {}", postId, e);
            throw new AnalyticsException(AnalyticsErrorCode.EMOTION_ANALYSIS_ERROR, "Failed to analyze emotions", e);
        }
    }
    
    /**
     * 댓글 메트릭을 저장합니다.
     */
    private void saveCommentMetrics(Long postId, List<PostCommentsQueryResponse> comments, 
                                  List<AiAnalysisResponse.IndividualResult> analysisResults) {
        
        Map<String, SentimentType> resultMap = analysisResults.stream()
            .collect(Collectors.toMap(
                AiAnalysisResponse.IndividualResult::getId,
                AiAnalysisResponse.IndividualResult::getResult
            ));
        
        List<SnsPostCommentMetric> metrics = comments.stream()
            .map(comment -> {
                SentimentType sentiment = resultMap.get(comment.getCommentId());
                // sentiment가 null이어도 허용
                
                return SnsPostCommentMetric.builder()
                    .snsCommentId(comment.getCommentId())
                    .postId(postId)
                    .authorId(comment.getAuthorId())
                    .content(comment.getText())  // text 필드 사용
                    .likeCount(comment.getLikeCount())
                    .publishedAt(comment.getPublishedAt())
                    .sentiment(sentiment)  // null 허용
                    .build();
            })
            .collect(Collectors.toList());
        
        commentMetricRepository.saveAll(metrics);
    }
    
    /**
     * 키워드를 저장합니다.
     */
    private void saveKeywords(Long postId, AiAnalysisResponse.Keywords keywords) {
        // 기존 키워드 삭제
        keywordRepository.deleteByPostId(postId);
        
        List<PostCommentKeyword> keywordEntities = new java.util.ArrayList<>();
        
        // 긍정 키워드 저장
        if (keywords.getPositive() != null) {
            keywordEntities.addAll(keywords.getPositive().stream()
                .map(keyword -> PostCommentKeyword.builder()
                    .postId(postId)
                    .keyword(keyword)
                    .sentiment(SentimentType.POSITIVE)
                    .build())
                .collect(Collectors.toList()));
        }
        
        // 부정 키워드 저장
        if (keywords.getNegative() != null) {
            keywordEntities.addAll(keywords.getNegative().stream()
                .map(keyword -> PostCommentKeyword.builder()
                    .postId(postId)
                    .keyword(keyword)
                    .sentiment(SentimentType.NEGATIVE)
                    .build())
                .collect(Collectors.toList()));
        }
        
        if (!keywordEntities.isEmpty()) {
            keywordRepository.saveAll(keywordEntities);
        }
    }
    

}
