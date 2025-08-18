package kt.aivle.analytics.application.port.in;

import java.util.List;

import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimeAccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimePostMetricsResponse;
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostMetricsQueryRequest;

public interface AnalyticsQueryUseCase {
    
    // 로컬 DB 데이터 조회 (date 파라미터 필수)
    List<PostMetricsQueryResponse> getPostMetrics(String userId, PostMetricsQueryRequest request);
    
    List<AccountMetricsQueryResponse> getAccountMetrics(String userId, AccountMetricsQueryRequest request);
    
    List<PostCommentsQueryResponse> getPostComments(String userId, PostCommentsQueryRequest request);
    
    // 실시간 YouTube API 데이터 조회 (date 파라미터 없음)
    List<RealtimePostMetricsResponse> getRealtimePostMetrics(String userId, PostMetricsQueryRequest request);
    
    List<RealtimeAccountMetricsResponse> getRealtimeAccountMetrics(String userId, AccountMetricsQueryRequest request);
    
    List<PostCommentsQueryResponse> getRealtimePostComments(String userId, PostCommentsQueryRequest request);
}
