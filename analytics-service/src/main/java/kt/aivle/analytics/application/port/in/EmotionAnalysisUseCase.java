package kt.aivle.analytics.application.port.in;

public interface EmotionAnalysisUseCase {
    
    /**
     * sentiment가 null인 모든 댓글들에 대해 감정분석을 수행합니다.
     */
    void analyzeAllNullSentimentComments();
    
    /**
     * 특정 게시물의 sentiment가 null인 댓글들에 대해 감정분석을 수행합니다.
     */
    void analyzeNullSentimentCommentsByPostId(Long postId);
}
