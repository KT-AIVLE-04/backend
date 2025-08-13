package kt.aivle.analytics.adapter.in.web.dto;

public record DashboardStatistics(
    Long totalViews,
    Long totalLikes,
    Long totalComments,
    Double averageEngagementRate,
    Long totalVideos,
    Double growthRate
) {}
