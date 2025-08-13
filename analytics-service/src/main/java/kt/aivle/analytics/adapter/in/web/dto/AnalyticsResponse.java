package kt.aivle.analytics.adapter.in.web.dto;

public record AnalyticsResponse(
    Long totalViews,
    Long totalLikes,
    Long totalComments,
    Double averageEngagementRate,
    Long totalVideos,
    Double growthRate
) {
    public static AnalyticsResponse from(DashboardStatistics stats) {
        return new AnalyticsResponse(
            stats.totalViews(),
            stats.totalLikes(),
            stats.totalComments(),
            stats.averageEngagementRate(),
            stats.totalVideos(),
            stats.growthRate()
        );
    }
}
