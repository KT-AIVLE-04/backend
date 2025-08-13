package kt.aivle.analytics.adapter.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kt.aivle.analytics.adapter.in.web.dto.AnalysisRequest;
import kt.aivle.analytics.adapter.in.web.dto.AnalysisResultResponse;
import kt.aivle.analytics.adapter.in.web.dto.AnalyticsResponse;
import kt.aivle.analytics.adapter.in.web.dto.DashboardStatisticsRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsResponse;
import kt.aivle.analytics.adapter.in.web.mapper.AnalyticsCommandMapper;
import kt.aivle.analytics.application.port.in.AnalyticsUseCase;
import kt.aivle.analytics.application.port.in.command.AnalyzeOptimalTimeCommand;
import kt.aivle.analytics.application.port.in.command.AnalyzeSentimentCommand;
import kt.aivle.analytics.application.port.in.command.AnalyzeTrendsCommand;
import kt.aivle.analytics.application.port.in.command.CollectMetricsCommand;
import kt.aivle.analytics.application.port.in.command.GenerateReportCommand;
import kt.aivle.analytics.application.port.in.command.GetDashboardStatisticsCommand;
import kt.aivle.analytics.application.port.in.command.GetPostMetricsCommand;
import kt.aivle.analytics.application.port.in.command.GetTopContentCommand;
import kt.aivle.analytics.application.port.in.command.RefreshTokenCommand;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final AnalyticsUseCase analyticsUseCase;
    private final AnalyticsCommandMapper analyticsCommandMapper;
    
    @PostMapping("/collect-metrics")
    public ResponseEntity<ApiResponse<Void>> collectMetrics(
            @RequestHeader("X-USER-ID") String userId,
            @RequestParam String snsType) {
        
        // TODO: 실제로는 post-service에서 게시글 정보를 받아와야 함
        // 현재는 임시로 빈 리스트로 처리
        CollectMetricsCommand command = analyticsCommandMapper.toCollectMetricsCommand(snsType, userId, List.of());
        analyticsUseCase.collectMetrics(command);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, null));
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getDashboardStatistics(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody DashboardStatisticsRequest request) {
        
        GetDashboardStatisticsCommand command = analyticsCommandMapper.toGetDashboardStatisticsCommand(request, userId);
        AnalyticsResponse response = analyticsUseCase.getDashboardStatistics(command);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @GetMapping("/video-metrics")
    public ResponseEntity<ApiResponse<List<PostMetricsResponse>>> getVideoMetrics(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PostMetricsRequest request) {
        
        GetPostMetricsCommand command = analyticsCommandMapper.toGetPostMetricsCommand(request, userId);
        List<PostMetricsResponse> response = analyticsUseCase.getPostMetrics(command);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @GetMapping("/top-content")
    public ResponseEntity<ApiResponse<List<PostMetricsResponse>>> getTopPerformingContent(
            @RequestHeader("X-USER-ID") String userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        GetTopContentCommand command = new GetTopContentCommand(userId, limit);
        List<PostMetricsResponse> response = analyticsUseCase.getTopPerformingContent(command);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @PostMapping("/analyze-sentiment")
    public ResponseEntity<ApiResponse<AnalysisResultResponse>> analyzeSentiment(
            @RequestHeader("X-USER-ID") String userId,
            @RequestParam String videoId) {
        
        AnalyzeSentimentCommand command = analyticsCommandMapper.toAnalyzeSentimentCommand(videoId, userId);
        AnalysisResultResponse response = analyticsUseCase.analyzeSentiment(command);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @PostMapping("/analyze-trends")
    public ResponseEntity<ApiResponse<AnalysisResultResponse>> analyzeTrends(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody AnalysisRequest request) {
        
        AnalyzeTrendsCommand command = analyticsCommandMapper.toAnalyzeTrendsCommand(request, userId);
        AnalysisResultResponse response = analyticsUseCase.analyzeTrends(command);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @PostMapping("/analyze-optimal-time")
    public ResponseEntity<ApiResponse<AnalysisResultResponse>> analyzeOptimalPostingTime(
            @RequestHeader("X-USER-ID") String userId) {
        
        AnalyzeOptimalTimeCommand command = new AnalyzeOptimalTimeCommand(userId);
        AnalysisResultResponse response = analyticsUseCase.analyzeOptimalPostingTime(command);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @PostMapping("/generate-report")
    public ResponseEntity<ApiResponse<AnalysisResultResponse>> generateReport(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody AnalysisRequest request) {
        
        GenerateReportCommand command = analyticsCommandMapper.toGenerateReportCommand(request, userId);
        AnalysisResultResponse response = analyticsUseCase.generateReport(command);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Void>> refreshToken(
            @RequestHeader("X-USER-ID") String userId,
            @RequestParam String snsType) {
        
        RefreshTokenCommand command = analyticsCommandMapper.toRefreshTokenCommand(snsType, userId);
        analyticsUseCase.refreshToken(command);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, null));
    }
}
