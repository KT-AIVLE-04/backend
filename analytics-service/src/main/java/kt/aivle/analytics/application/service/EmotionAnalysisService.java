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
            log.info("🧠 감정분석 시작 - postId: {}, 댓글 수: {}", postId, comments.size());
            
            // AI 분석 수행 (기존 키워드는 AiAnalysisAdapter 내부에서 조회)
            AiAnalysisResponse aiResponse = aiAnalysisPort.analyzeComments(comments, postId);
            
            // null 체크 추가
            if (aiResponse == null || aiResponse.getIndividual_results() == null) {
                log.error("❌ AI 분석 응답이 유효하지 않습니다 - postId: {}", postId);
                throw new BusinessException(AnalyticsErrorCode.EMOTION_ANALYSIS_ERROR);
            }
            
            // 감정분석 결과 저장
            saveCommentMetrics(postId, comments, aiResponse.getIndividual_results());
            
            // 키워드 저장
            saveKeywords(postId, aiResponse.getKeywords());
            
            log.info("✅ 감정분석 완료 및 저장 - postId: {}", postId);
            
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
                result -> result.getId(), // AI 서버에서 받은 ID는 DB ID (이제 Long 타입)
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
        
        log.info("💾 감정 업데이트 완료 - postId: {}, 성공: {}/{}", postId, updatedCount, comments.size());
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
