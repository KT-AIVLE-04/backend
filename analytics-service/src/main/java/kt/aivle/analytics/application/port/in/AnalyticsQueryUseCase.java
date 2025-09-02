package kt.aivle.analytics.application.port.in;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.EmotionAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.ReportResponse;
import kt.aivle.analytics.adapter.in.websocket.dto.WebSocketResponseMessage;

public interface AnalyticsQueryUseCase {
    
    // 실시간 YouTube API 데이터 조회 (date 파라미터 없음)
    PostMetricsResponse getRealtimePostMetrics(Long userId, Long accountId, Long postId);
    
    AccountMetricsResponse getRealtimeAccountMetrics(Long userId, Long accountId);
    
    List<PostCommentsResponse> getRealtimePostComments(Long userId, Long accountId, Long postId, Integer page, Integer size);

    // 히스토리 데이터 조회 (date 파라미터 필수)
    PostMetricsResponse getHistoricalPostMetrics(Long userId, String dateStr, Long accountId, Long postId);
    
    AccountMetricsResponse getHistoricalAccountMetrics(Long userId, String dateStr, Long accountId);
    
    List<PostCommentsResponse> getHistoricalPostComments(Long userId, String dateStr, Long accountId, Long postId, Integer page, Integer size);
    
    EmotionAnalysisResponse getHistoricalEmotionAnalysis(Long userId, String dateStr, Long accountId, Long postId);
    
    // AI 보고서 생성 (캐시 포함)
    ReportResponse generateReport(Long userId, Long accountId, Long postId, Long storeId);
    
    // 통합된 비동기 AI 보고서 생성 (WebSocket용) - 캐시 확인 포함
    CompletableFuture<WebSocketResponseMessage<ReportResponse>> generateReportAsync(Long accountId, Long postId, Long storeId);
}
