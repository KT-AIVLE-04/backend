package kt.aivle.analytics.adapter.in.web.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import kt.aivle.analytics.adapter.in.web.dto.AnalysisRequest;
import kt.aivle.analytics.adapter.in.web.dto.DashboardStatisticsRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsRequest;
import kt.aivle.analytics.application.port.in.command.AnalyzeOptimalTimeCommand;
import kt.aivle.analytics.application.port.in.command.AnalyzeSentimentCommand;
import kt.aivle.analytics.application.port.in.command.AnalyzeTrendsCommand;
import kt.aivle.analytics.application.port.in.command.CollectMetricsCommand;
import kt.aivle.analytics.application.port.in.command.GenerateReportCommand;
import kt.aivle.analytics.application.port.in.command.GetDashboardStatisticsCommand;
import kt.aivle.analytics.application.port.in.command.GetPostMetricsCommand;
import kt.aivle.analytics.application.port.in.command.RefreshTokenCommand;

@Mapper(componentModel = "spring")
public interface AnalyticsCommandMapper {
    
    CollectMetricsCommand toCollectMetricsCommand(String snsType, String userId, List<kt.aivle.analytics.adapter.in.event.dto.SocialPostResponseEvent.SocialPost> socialPosts);
    
    GetDashboardStatisticsCommand toGetDashboardStatisticsCommand(DashboardStatisticsRequest request, String userId);
    
    GetPostMetricsCommand toGetPostMetricsCommand(PostMetricsRequest request, String userId);
    
    AnalyzeSentimentCommand toAnalyzeSentimentCommand(String videoId, String userId);
    
    AnalyzeTrendsCommand toAnalyzeTrendsCommand(AnalysisRequest request, String userId);
    
    AnalyzeOptimalTimeCommand toAnalyzeOptimalTimeCommand(String userId);
    
    GenerateReportCommand toGenerateReportCommand(AnalysisRequest request, String userId);
    
    RefreshTokenCommand toRefreshTokenCommand(String snsType, String userId);
}
