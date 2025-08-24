package kt.aivle.analytics.application.port.in;

import java.util.List;

import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.EmotionAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;

public interface AnalyticsQueryUseCase {
    
    // 실시간 YouTube API 데이터 조회 (date 파라미터 없음)
    PostMetricsResponse getRealtimePostMetrics(String userId, String snsType, String postId);
    
    AccountMetricsResponse getRealtimeAccountMetrics(String userId, String snsType);
    
    List<PostCommentsResponse> getRealtimePostComments(String userId, String snsType, String postId, Integer page, Integer size);

    // 히스토리 데이터 조회 (date 파라미터 필수)
    PostMetricsResponse getHistoricalPostMetrics(String userId, String dateStr, String snsType, String postId);
    
    AccountMetricsResponse getHistoricalAccountMetrics(String userId, String dateStr, String snsType);
    
    List<PostCommentsResponse> getHistoricalPostComments(String userId, String dateStr, String snsType, String postId, Integer page, Integer size);
    
    EmotionAnalysisResponse getHistoricalEmotionAnalysis(String userId, String dateStr, String snsType, String postId);
}
