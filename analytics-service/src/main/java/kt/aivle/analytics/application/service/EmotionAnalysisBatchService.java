package kt.aivle.analytics.application.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import kt.aivle.analytics.application.port.in.EmotionAnalysisUseCase;
import kt.aivle.analytics.application.port.out.repository.SnsPostCommentMetricRepositoryPort;
import kt.aivle.analytics.domain.entity.SnsPostCommentMetric;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionAnalysisBatchService implements EmotionAnalysisUseCase {
    
    private final SnsPostCommentMetricRepositoryPort commentMetricRepository;
    private final EmotionAnalysisService emotionAnalysisService;
    private final BatchJobMonitor batchJobMonitor;
    
    @Value("${app.emotion.analysis.batch-size:50}")
    private int batchSize;
    
    // AI 분석용 전용 스레드 풀 (최대 3개 동시 실행)
    private final ExecutorService aiAnalysisExecutor = Executors.newFixedThreadPool(3);
    
    @Override
    public void analyzeAllNullSentimentComments() {
        String jobName = "emotion-analysis-all";
        batchJobMonitor.recordJobStart(jobName);
        
        try {
            log.info("🧠 전체 null sentiment 댓글 감정분석 시작");
            
            // sentiment가 null인 댓글들을 postId로 그룹화하여 조회
            Map<Long, List<SnsPostCommentMetric>> commentsByPostId = commentMetricRepository.findCommentsWithNullSentimentGroupedByPostId();
            
            if (commentsByPostId.isEmpty()) {
                log.info("✅ 분석할 null sentiment 댓글이 없습니다.");
                batchJobMonitor.recordJobSuccess(jobName);
                return;
            }
            
            int totalComments = commentsByPostId.values().stream().mapToInt(List::size).sum();
            log.info("📊 총 {}개 게시물에서 {}개의 null sentiment 댓글 발견", 
                commentsByPostId.size(), totalComments);
            
            // 작업 진행률 업데이트
            batchJobMonitor.recordJobProgress(jobName, 0, totalComments);
            
            // 각 게시물별로 감정분석 수행
            int processedComments = 0;
            int processedPosts = 0;
            for (Map.Entry<Long, List<SnsPostCommentMetric>> entry : commentsByPostId.entrySet()) {
                Long postId = entry.getKey();
                List<SnsPostCommentMetric> comments = entry.getValue();
                
                try {
                    log.info("🧠 게시물 {} 감정분석 시작 - 댓글 수: {}", postId, comments.size());
                    
                    // 배치 크기로 나누어 처리
                    int batchProcessed = processCommentsInBatches(postId, comments);
                    processedComments += batchProcessed;
                    
                    // 진행률 업데이트
                    batchJobMonitor.recordJobProgress(jobName, processedComments, totalComments);
                    
                    processedPosts++;
                    log.info("✅ 게시물 {} 감정분석 완료 ({}/{}) - 진행률: {}/{}", 
                        postId, processedPosts, commentsByPostId.size(), processedComments, totalComments);
                    
                } catch (Exception e) {
                    log.error("❌ 게시물 {} 감정분석 실패: {}", postId, e.getMessage(), e);
                    // 개별 게시물 실패는 다른 게시물에 영향을 주지 않음
                }
            }
            
            log.info("🎉 전체 감정분석 완료 - 처리된 게시물: {}/{}, 댓글: {}/{}", 
                processedPosts, commentsByPostId.size(), processedComments, totalComments);
            
            batchJobMonitor.recordJobSuccess(jobName);
            
        } catch (Exception e) {
            log.error("❌ 전체 감정분석 실패: {}", e.getMessage(), e);
            batchJobMonitor.recordJobFailure(jobName, e.getMessage());
            throw new BusinessException(AnalyticsErrorCode.EMOTION_ANALYSIS_ERROR);
        }
    }
    
    @Override
    public void analyzeNullSentimentCommentsByPostId(Long postId) {
        String jobName = "emotion-analysis-post-" + postId;
        batchJobMonitor.recordJobStart(jobName);
        
        try {
            log.info("🧠 게시물 {} null sentiment 댓글 감정분석 시작", postId);
            
            // DB 레벨에서 sentiment가 null인 댓글들만 조회 (최적화)
            List<SnsPostCommentMetric> comments = commentMetricRepository.findByPostIdAndSentimentIsNull(postId);
            
            if (comments.isEmpty()) {
                log.info("✅ 게시물 {}에 분석할 null sentiment 댓글이 없습니다.", postId);
                batchJobMonitor.recordJobSuccess(jobName);
                return;
            }
            
            log.info("📊 게시물 {}에서 {}개의 null sentiment 댓글 발견", postId, comments.size());
            
            // 작업 진행률 업데이트
            batchJobMonitor.recordJobProgress(jobName, 0, comments.size());
            
            // 배치 크기로 나누어 처리
            int processedComments = processCommentsInBatches(postId, comments);
            
            // 진행률 업데이트
            batchJobMonitor.recordJobProgress(jobName, processedComments, comments.size());
            
            log.info("✅ 게시물 {} 감정분석 완료 - 처리된 댓글: {}/{}", postId, processedComments, comments.size());
            
            batchJobMonitor.recordJobSuccess(jobName);
            
        } catch (Exception e) {
            log.error("❌ 게시물 {} 감정분석 실패: {}", postId, e.getMessage(), e);
            batchJobMonitor.recordJobFailure(jobName, e.getMessage());
            throw new BusinessException(AnalyticsErrorCode.EMOTION_ANALYSIS_ERROR);
        }
    }
    
    /**
     * 댓글들을 배치 크기로 나누어 감정분석 수행
     * @return 처리된 댓글 수
     */
    private int processCommentsInBatches(Long postId, List<SnsPostCommentMetric> comments) {
        int totalComments = comments.size();
        int processedComments = 0;
        
        for (int i = 0; i < totalComments; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalComments);
            List<SnsPostCommentMetric> batch = comments.subList(i, endIndex);
            
            try {
                log.info("🧠 게시물 {} 배치 처리 중 - {}/{} ({}-{})", 
                    postId, processedComments + batch.size(), totalComments, i + 1, endIndex);
                
                // 동기적으로 감정분석 수행 (배치 단위로)
                emotionAnalysisService.analyzeAndSaveEmotions(postId, batch);
                
                processedComments += batch.size();
                log.info("✅ 게시물 {} 배치 완료 - 진행률: {}/{}", postId, processedComments, totalComments);
                
            } catch (Exception e) {
                log.error("❌ 게시물 {} 배치 처리 실패 ({}-{}): {}", postId, i + 1, endIndex, e.getMessage(), e);
                // 개별 배치 실패는 다른 배치에 영향을 주지 않음
            }
        }
        
        return processedComments;
    }
}
