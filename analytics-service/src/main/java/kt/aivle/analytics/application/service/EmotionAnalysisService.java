package kt.aivle.analytics.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import kt.aivle.analytics.application.port.out.dto.AiAnalysisResponse;
import kt.aivle.analytics.application.port.out.infrastructure.AiAnalysisPort;
import kt.aivle.analytics.application.port.out.repository.PostCommentKeywordRepositoryPort;
import kt.aivle.analytics.application.port.out.repository.SnsPostCommentMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.PostCommentKeyword;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.domain.model.SentimentType;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionAnalysisService {
    
    private final SnsPostCommentMetricRepositoryPort commentMetricRepository;
    private final PostCommentKeywordRepositoryPort keywordRepository;
    private final AiAnalysisPort aiAnalysisPort;
    
    /**
     * 댓글 감정분석을 수행하고 결과를 저장합니다.
     */
    public void analyzeAndSaveEmotions(Long postId, List<SnsPostCommentMetric> comments) {
        try {
                    // AI 분석 수행 (기존 키워드는 AiAnalysisAdapter 내부에서 조회)
        AiAnalysisResponse aiResponse = aiAnalysisPort.analyzeComments(comments, postId);
            
            // 감정분석 결과 저장
            saveCommentMetrics(postId, comments, aiResponse.getEmotionAnalysis().getIndividualResults());
            
            // 키워드 저장
            saveKeywords(postId, aiResponse.getKeywords());
            
            log.info("Emotion analysis completed and saved for postId: {}", postId);
            
        } catch (Exception e) {
            log.error("Failed to analyze and save emotions for postId: {}", postId, e);
            throw new BusinessException(AnalyticsErrorCode.EMOTION_ANALYSIS_ERROR);
        }
    }
    
    /**
     * 댓글의 sentiment를 업데이트합니다.
     */
    private void saveCommentMetrics(Long postId, List<SnsPostCommentMetric> comments, 
                                  List<AiAnalysisResponse.IndividualResult> analysisResults) {
        
        // AI 응답을 DB ID 기준으로 매핑 (AI 서버가 DB ID를 기준으로 응답함)
        Map<Long, SentimentType> resultMap = analysisResults.stream()
            .collect(Collectors.toMap(
                result -> Long.valueOf(result.getId()), // AI 서버에서 받은 ID는 DB ID
                AiAnalysisResponse.IndividualResult::getResult
            ));
        
        int updatedCount = 0;
        for (SnsPostCommentMetric comment : comments) {
            try {
                // AI 분석 결과에서 해당 댓글의 sentiment 조회
                SentimentType sentiment = resultMap.get(comment.getId());
                
                if (sentiment != null) {
                    // 기존 댓글의 sentiment 필드만 업데이트
                    commentMetricRepository.updateSentimentById(comment.getId(), sentiment);
                    updatedCount++;
                    log.debug("Updated sentiment for comment ID: {}, sentiment: {}", comment.getId(), sentiment);
                } else {
                    log.warn("No sentiment result found for comment ID: {}", comment.getId());
                }
                
            } catch (Exception e) {
                log.error("Failed to update sentiment for comment ID: {}", comment.getId(), e);
                // 개별 댓글 업데이트 실패는 다른 댓글에 영향을 주지 않음
            }
        }
        
        log.info("Updated sentiment for {} out of {} comments in postId: {}", updatedCount, comments.size(), postId);
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
                .toList());
        }
        
        // 부정 키워드 저장
        if (keywords.getNegative() != null) {
            keywordEntities.addAll(keywords.getNegative().stream()
                .map(keyword -> PostCommentKeyword.builder()
                    .postId(postId)
                    .keyword(keyword)
                    .sentiment(SentimentType.NEGATIVE)
                    .build())
                .toList());
        }
        
        if (!keywordEntities.isEmpty()) {
            keywordRepository.saveAll(keywordEntities);
        }
    }
    

}
