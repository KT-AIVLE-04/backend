package kt.aivle.analytics.application.port.in;

import java.util.concurrent.CompletableFuture;

import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.EmotionAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsPageResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.ReportResponse;

public interface AnalyticsQueryUseCase {
    
    // 실시간 YouTube API 데이터 조회 (date 파라미터 없음)
    PostMetricsResponse getRealtimePostMetrics(Long userId, Long accountId, Long postId);
    
    AccountMetricsResponse getRealtimeAccountMetrics(Long userId, Long accountId);
    
    PostCommentsPageResponse getRealtimePostComments(Long userId, Long accountId, Long postId, String pageToken, Integer size);

    // 히스토리 데이터 조회 (date 파라미터 필수)
    PostMetricsResponse getHistoricalPostMetrics(Long userId, String dateStr, Long accountId, Long postId);
    
    AccountMetricsResponse getHistoricalAccountMetrics(Long userId, String dateStr, Long accountId);
    
    EmotionAnalysisResponse getHistoricalEmotionAnalysis(Long userId, String dateStr, Long accountId, Long postId);
    
    // AI 보고서 생성 (캐시 포함)
    ReportResponse generateReport(Long userId, Long accountId, Long postId, Long storeId);
    
    // 통합된 비동기 AI 보고서 생성 (WebSocket용) - 캐시 확인 포함
    CompletableFuture<ReportResponse> generateReportAsync(Long accountId, Long postId, Long storeId, ProgressCallback callback);
}
