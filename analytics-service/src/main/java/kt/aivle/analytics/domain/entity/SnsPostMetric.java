package kt.aivle.analytics.domain.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class SnsPostMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "sns_post_id")
    private String snsPostId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "likes")
    private Long likes;

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

    @Builder
    public SnsPostMetric(Long id, Long postId, String snsPostId, Long accountId, Long likes, Long dislikes, Long comments, Long shares, Long views, LocalDateTime crawledAt) {
        this.id = id;
        this.postId = postId;
        this.snsPostId = snsPostId;
        this.accountId = accountId;
        this.likes = likes;
        this.dislikes = dislikes;
        this.comments = comments;
        this.shares = shares;
        this.views = views;
        this.crawledAt = crawledAt;
    }
}
