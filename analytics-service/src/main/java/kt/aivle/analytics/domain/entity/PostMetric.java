package kt.aivle.analytics.domain.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kt.aivle.analytics.domain.model.SnsType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_metric")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostMetric extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "social_post_id", nullable = false)
    private Long socialPostId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sns_type", nullable = false)
    private SnsType snsType;
    
    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;
    
    @Column(name = "view_count")
    private Long viewCount;
    
    @Column(name = "like_count")
    private Long likeCount;
    
    @Column(name = "comment_count")
    private Long commentCount;
    
    @Column(name = "share_count")
    private Long shareCount;
    
    @Column(name = "subscriber_count")
    private Long subscriberCount;
    
    @Column(name = "engagement_rate")
    private Double engagementRate;
    
    public void updateMetrics(Long viewCount, Long likeCount, Long commentCount, Long shareCount) {
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.shareCount = shareCount;
        this.engagementRate = calculateEngagementRate();
    }
    
    private Double calculateEngagementRate() {
        if (viewCount == null || viewCount == 0) return 0.0;
        long totalEngagement = (likeCount != null ? likeCount : 0) + 
                             (commentCount != null ? commentCount : 0) + 
                             (shareCount != null ? shareCount : 0);
        return (double) totalEngagement / viewCount * 100;
    }
}
