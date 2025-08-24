package kt.aivle.analytics.application.port.in;

import java.time.LocalDate;
import java.util.List;

import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.EmotionAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.domain.model.SnsType;

public interface AnalyticsQueryUseCase {
    
    // 로컬 DB 데이터 조회 (date 파라미터 필수)
    List<PostMetricsResponse> getPostMetrics(String userId, PostMetricsQueryRequest request);
    
    AccountMetricsResponse getAccountMetrics(String userId, AccountMetricsQueryRequest request);
    
    List<PostCommentsResponse> getPostComments(String userId, PostCommentsQueryRequest request);
    
    // 실시간 YouTube API 데이터 조회 (date 파라미터 없음)
    List<PostMetricsResponse> getRealtimePostMetrics(String userId, PostMetricsQueryRequest request);
    
    AccountMetricsResponse getRealtimeAccountMetrics(String userId, AccountMetricsQueryRequest request);
    
    List<PostCommentsResponse> getRealtimePostComments(String userId, PostCommentsQueryRequest request);

    // 히스토리 감정분석 데이터 조회 (날짜 기반)
    EmotionAnalysisResponse getHistoricalEmotionAnalysis(String userId, String postId, SnsType snsType, LocalDate date);
}
