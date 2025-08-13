package kt.aivle.analytics.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kt.aivle.analytics.adapter.out.event.dto.SnsTokenRequestEvent;
import kt.aivle.analytics.adapter.in.web.dto.AnalysisResultResponse;
import kt.aivle.analytics.adapter.in.web.dto.AnalyticsResponse;
import kt.aivle.analytics.adapter.in.web.dto.DashboardStatistics;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsResponse;
import kt.aivle.analytics.adapter.out.event.SnsTokenEventProducer;
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
import kt.aivle.analytics.application.port.out.ExternalApiPort;
import kt.aivle.analytics.domain.entity.AnalysisResult;
import kt.aivle.analytics.domain.entity.PostMetric;
import kt.aivle.analytics.domain.model.AnalysisType;
import kt.aivle.analytics.domain.model.SnsType;
import kt.aivle.analytics.domain.port.out.CommentRepositoryPort;
import kt.aivle.analytics.domain.port.out.PostMetricRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalyticsService implements AnalyticsUseCase {
    
    private final PostMetricRepositoryPort postMetricRepositoryPort;
    private final CommentRepositoryPort commentRepositoryPort;
    private final ExternalApiPort externalApiPort;
    private final SnsTokenEventProducer snsTokenEventProducer;
    
    @Override
    public void collectMetrics(CollectMetricsCommand command) {
        log.info("메트릭 수집 시작: userId={}, snsType={}, postsCount={}", 
                command.userId(), command.snsType(), command.socialPosts().size());
        
        // post-service에서 받은 게시글 정보를 기반으로 메트릭 수집
        if (command.snsType() == SnsType.YOUTUBE) {
            collectYouTubeMetrics(command.userId(), command.socialPosts());
        }
    }
    
    private void collectYouTubeMetrics(String userId, List<kt.aivle.analytics.adapter.in.event.dto.SocialPostResponseEvent.SocialPost> posts) {
        try {
            // SNS 토큰 정보 요청
            String requestId = UUID.randomUUID().toString();
            SnsTokenRequestEvent tokenRequest = new SnsTokenRequestEvent(requestId, userId, "youtube");
            snsTokenEventProducer.sendRequest(tokenRequest);
            
            // TODO: 토큰 응답을 기다리는 로직 구현 필요
            // 현재는 임시로 하드코딩된 토큰 사용
            String accessToken = "temporary_token";
            
            for (kt.aivle.analytics.adapter.in.event.dto.SocialPostResponseEvent.SocialPost post : posts) {
                // YouTube Analytics API를 사용하여 비디오 메트릭 조회
                ExternalApiPort.VideoMetrics metrics = externalApiPort.getYouTubeVideoMetrics(accessToken, post.getSnsPostId());
                
                // PostMetric으로 저장 (VideoMetric 대신)
                kt.aivle.analytics.domain.entity.PostMetric postMetric = kt.aivle.analytics.domain.entity.PostMetric.builder()
                    .socialPostId(post.getId())
                    .userId(userId)
                    .snsType(SnsType.YOUTUBE)
                    .metricDate(LocalDate.now())
                    .viewCount(metrics.viewCount())
                    .likeCount(metrics.likeCount())
                    .commentCount(metrics.commentCount())
                    .shareCount(metrics.shareCount())
                    .build();
                
                postMetric.updateMetrics(metrics.viewCount(), metrics.likeCount(), metrics.commentCount(), metrics.shareCount());
                postMetricRepositoryPort.save(postMetric);
                
                // 댓글 수집
                List<ExternalApiPort.CommentData> comments = externalApiPort.getYouTubeComments(accessToken, post.getSnsPostId());
                for (ExternalApiPort.CommentData commentData : comments) {
                    kt.aivle.analytics.domain.entity.Comment comment = kt.aivle.analytics.domain.entity.Comment.builder()
                        .socialPostId(post.getId())
                        .userId(userId)
                        .snsType(SnsType.YOUTUBE)
                        .content(commentData.content())
                        .crawledAt(LocalDateTime.now())
                        .build();
                    commentRepositoryPort.save(comment);
                }
            }
            
            log.info("YouTube 메트릭 수집 완료: userId={}, postsCount={}", userId, posts.size());
            
        } catch (Exception e) {
            log.error("YouTube 메트릭 수집 실패: userId={}", userId, e);
        }
    }
    
    @Override
    public List<PostMetricsResponse> getPostMetrics(GetPostMetricsCommand command) {
        List<PostMetric> metrics = postMetricRepositoryPort.findByUserIdAndSocialPostIdAndDateRange(
            command.userId(), command.socialPostId(), command.startDate(), command.endDate());
        return metrics.stream().map(PostMetricsResponse::from).toList();
    }
    
    @Override
    public AnalyticsResponse getDashboardStatistics(GetDashboardStatisticsCommand command) {
        List<PostMetric> metrics = postMetricRepositoryPort.findByUserIdAndDateRange(
            command.userId(), command.startDate(), command.endDate());
        
        long totalViews = metrics.stream().mapToLong(m -> m.getViewCount() != null ? m.getViewCount() : 0).sum();
        long totalLikes = metrics.stream().mapToLong(m -> m.getLikeCount() != null ? m.getLikeCount() : 0).sum();
        long totalComments = metrics.stream().mapToLong(m -> m.getCommentCount() != null ? m.getCommentCount() : 0).sum();
        double averageEngagementRate = metrics.stream().mapToDouble(m -> m.getEngagementRate() != null ? m.getEngagementRate() : 0.0).average().orElse(0.0);
        long totalPosts = metrics.stream().map(PostMetric::getSocialPostId).distinct().count();
        
        DashboardStatistics stats = new DashboardStatistics(
            totalViews, totalLikes, totalComments, averageEngagementRate, totalPosts, 0.0);
        
        return AnalyticsResponse.from(stats);
    }
    
    @Override
    public AnalysisResultResponse analyzeSentiment(AnalyzeSentimentCommand command) {
        // 실제 댓글 데이터 조회
        List<kt.aivle.analytics.domain.entity.Comment> comments = commentRepositoryPort.findBySocialPostId(Long.parseLong(command.videoId()));
        List<String> commentContents = comments.stream()
            .map(kt.aivle.analytics.domain.entity.Comment::getContent)
            .toList();
        
        ExternalApiPort.SentimentAnalysisResult sentimentResult = externalApiPort.analyzeSentiment(command.videoId(), commentContents);
        
        AnalysisResult result = AnalysisResult.builder()
            .userId(command.userId())
            .videoId(command.videoId())
            .snsType(SnsType.YOUTUBE)
            .analysisType(AnalysisType.SENTIMENT_ANALYSIS)
            .periodStart(LocalDateTime.now())
            .periodEnd(LocalDateTime.now())
            .score(sentimentResult.positiveScore())
            .summary(sentimentResult.summary())
            .detailedAnalysis("{\"positive\": " + sentimentResult.positiveScore() + ", \"negative\": " + sentimentResult.negativeScore() + "}")
            .build();
        
        return AnalysisResultResponse.from(result);
    }
    
    @Override
    public AnalysisResultResponse analyzeTrends(AnalyzeTrendsCommand command) {
        List<PostMetric> metrics = postMetricRepositoryPort.findByUserIdAndDateRange(
            command.userId(), command.startDate(), command.endDate());
        
        Map<String, Object> trendData = Map.of(
            "totalViews", metrics.stream().mapToLong(m -> m.getViewCount() != null ? m.getViewCount() : 0).sum(),
            "totalLikes", metrics.stream().mapToLong(m -> m.getLikeCount() != null ? m.getLikeCount() : 0).sum(),
            "averageEngagement", metrics.stream().mapToDouble(m -> m.getEngagementRate() != null ? m.getEngagementRate() : 0.0).average().orElse(0.0)
        );
        
        ExternalApiPort.TrendAnalysisResult trendResult = externalApiPort.analyzeTrends(command.userId(), trendData);
        
        AnalysisResult result = AnalysisResult.builder()
            .userId(command.userId())
            .snsType(SnsType.YOUTUBE)
            .analysisType(AnalysisType.TREND_ANALYSIS)
            .periodStart(command.startDate().atStartOfDay())
            .periodEnd(command.endDate().atTime(23, 59, 59))
            .score(trendResult.confidence())
            .summary(trendResult.trend())
            .recommendations(trendResult.recommendation())
            .build();
        
        return AnalysisResultResponse.from(result);
    }
    
    @Override
    public AnalysisResultResponse analyzeOptimalPostingTime(AnalyzeOptimalTimeCommand command) {
        List<PostMetric> metrics = postMetricRepositoryPort.findByUserIdAndDateRange(
            command.userId(), LocalDate.now().minusDays(30), LocalDate.now());
        
        Map<String, Object> postingData = Map.of(
            "metrics", metrics,
            "userId", command.userId()
        );
        
        ExternalApiPort.OptimalTimeResult optimalResult = externalApiPort.analyzeOptimalPostingTime(command.userId(), postingData);
        
        AnalysisResult result = AnalysisResult.builder()
            .userId(command.userId())
            .snsType(SnsType.YOUTUBE)
            .analysisType(AnalysisType.OPTIMAL_POSTING_TIME)
            .periodStart(LocalDateTime.now())
            .periodEnd(LocalDateTime.now())
            .score(optimalResult.expectedEngagement())
            .summary("최적 게시 시간: " + optimalResult.optimalDay() + " " + optimalResult.optimalTime())
            .recommendations("예상 참여율: " + optimalResult.expectedEngagement() + "%")
            .build();
        
        return AnalysisResultResponse.from(result);
    }
    
    @Override
    public List<PostMetricsResponse> getTopPerformingContent(GetTopContentCommand command) {
        List<PostMetric> topContent = postMetricRepositoryPort.findTopPerformingByUserId(command.userId(), command.limit());
        return topContent.stream().map(PostMetricsResponse::from).toList();
    }
    
    @Override
    public AnalysisResultResponse generateReport(GenerateReportCommand command) {
        AnalyticsResponse stats = getDashboardStatistics(new GetDashboardStatisticsCommand(
            command.userId(), command.startDate(), command.endDate()));
        List<PostMetricsResponse> topContent = getTopPerformingContent(new GetTopContentCommand(command.userId(), 5));
        
        String reportSummary = String.format(
            "기간: %s ~ %s\n총 조회수: %,d\n총 좋아요: %,d\n평균 참여율: %.2f%%\n분석된 영상 수: %d",
            command.startDate(), command.endDate(), stats.totalViews(), stats.totalLikes(), stats.averageEngagementRate(), stats.totalVideos()
        );
        
        AnalysisResult result = AnalysisResult.builder()
            .userId(command.userId())
            .snsType(SnsType.YOUTUBE)
            .analysisType(AnalysisType.CONTENT_PERFORMANCE)
            .periodStart(command.startDate().atStartOfDay())
            .periodEnd(command.endDate().atTime(23, 59, 59))
            .score(stats.averageEngagementRate())
            .summary(reportSummary)
            .detailedAnalysis("{\"topContent\": " + topContent.size() + "}")
            .build();
        
        return AnalysisResultResponse.from(result);
    }
    
    @Override
    public void refreshToken(RefreshTokenCommand command) {
        log.info("토큰 갱신 요청: userId={}, snsType={}", command.userId(), command.snsType());
        // 토큰 갱신은 sns-service에서 처리하므로 여기서는 로그만 남김
    }
    
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void scheduledMetricsCollection() {
        log.info("스케줄된 메트릭 수집 시작");
        // 모든 사용자의 토큰을 조회하여 메트릭 수집
        // 실제 구현에서는 모든 사용자 목록을 가져와서 반복 처리
    }
}
