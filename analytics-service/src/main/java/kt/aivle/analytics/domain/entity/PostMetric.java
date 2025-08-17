package kt.aivle.analytics.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "POST_METRIC")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostMetric extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "post_id", nullable = false)
    private Long postId;
    
    @Column(name = "likes")
    private String likes;
    
    @Column(name = "dislikes")
    private Long dislikes;
    
    @Column(name = "comments")
    private Long comments;
    
    @Column(name = "shares")
    private Long shares;
    
    @Column(name = "views")
    private Long views;
    
    @Column(name = "crawled_at", nullable = false)
    private LocalDateTime crawledAt;
    
    public PostMetric(Long postId, String likes, Long dislikes, Long comments, Long shares, Long views, LocalDateTime crawledAt) {
        this.postId = postId;
        this.likes = likes;
        this.dislikes = dislikes;
        this.comments = comments;
        this.shares = shares;
        this.views = views;
        this.crawledAt = crawledAt;
    }
}
