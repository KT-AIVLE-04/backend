package kt.aivle.analytics.application.port.in;

import java.util.List;

import kt.aivle.analytics.adapter.in.web.dto.AnalysisResultResponse;
import kt.aivle.analytics.adapter.in.web.dto.AnalyticsResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsResponse;
import kt.aivle.analytics.application.port.in.command.AnalyzeOptimalTimeCommand;
import kt.aivle.analytics.application.port.in.command.AnalyzeSentimentCommand;
import kt.aivle.analytics.application.port.in.command.AnalyzeTrendsCommand;
import kt.aivle.analytics.application.port.in.command.CollectMetricsCommand;
import kt.aivle.analytics.application.port.in.command.GenerateReportCommand;
import kt.aivle.analytics.application.port.in.command.GetDashboardStatisticsCommand;
import kt.aivle.analytics.application.port.in.command.GetTopContentCommand;
import kt.aivle.analytics.application.port.in.command.GetPostMetricsCommand;
import kt.aivle.analytics.application.port.in.command.RefreshTokenCommand;

public interface AnalyticsUseCase {
    
    void collectMetrics(CollectMetricsCommand command);
    
    List<PostMetricsResponse> getPostMetrics(GetPostMetricsCommand command);
    
    AnalyticsResponse getDashboardStatistics(GetDashboardStatisticsCommand command);
    
    AnalysisResultResponse analyzeSentiment(AnalyzeSentimentCommand command);
    
    AnalysisResultResponse analyzeTrends(AnalyzeTrendsCommand command);
    
    AnalysisResultResponse analyzeOptimalPostingTime(AnalyzeOptimalTimeCommand command);
    
    List<PostMetricsResponse> getTopPerformingContent(GetTopContentCommand command);
    
    AnalysisResultResponse generateReport(GenerateReportCommand command);
    
    void refreshToken(RefreshTokenCommand command);
}
