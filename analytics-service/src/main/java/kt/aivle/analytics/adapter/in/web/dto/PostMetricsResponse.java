package kt.aivle.analytics.adapter.in.web.dto;

import java.time.LocalDateTime;

import kt.aivle.analytics.domain.entity.PostMetric;

public record PostMetricsResponse(
    Long socialPostId,
    String userId,
    String snsType,
    LocalDateTime metricDate,
    Long viewCount,
    Long likeCount,
    Long commentCount,
    Long shareCount,
    Long subscriberCount,
    Double engagementRate
) {
    public static PostMetricsResponse from(PostMetric metric) {
        return new PostMetricsResponse(
            metric.getSocialPostId(),
            metric.getUserId(),
            metric.getSnsType().name(),
            metric.getMetricDate().atStartOfDay(),
            metric.getViewCount(),
            metric.getLikeCount(),
            metric.getCommentCount(),
            metric.getShareCount(),
            metric.getSubscriberCount(),
            metric.getEngagementRate()
        );
    }
}
