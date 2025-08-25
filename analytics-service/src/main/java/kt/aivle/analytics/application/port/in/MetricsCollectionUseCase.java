package kt.aivle.analytics.application.port.in;

public interface MetricsCollectionUseCase {
    
    /**
     * 모든 SNS 계정의 메트릭을 수집합니다.
     */
    void collectAccountMetrics();
    
    /**
     * 모든 게시물의 메트릭을 수집합니다.
     */
    void collectPostMetrics();
    
    /**
     * 모든 게시물의 댓글을 수집합니다.
     */
    void collectPostComments();
    
    /**
     * 특정 SNS 계정의 메트릭을 수집합니다.
     */
    void collectAccountMetricsByAccountId(Long accountId);
    
    /**
     * 특정 게시물의 메트릭을 수집합니다.
     */
    void collectPostMetricsByPostId(Long postId);
    
    /**
     * 특정 게시물의 댓글을 수집합니다.
     */
    void collectPostCommentsByPostId(Long postId);
}
