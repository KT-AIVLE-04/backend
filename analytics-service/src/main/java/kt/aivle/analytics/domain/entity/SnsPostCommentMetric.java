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
public class SnsPostCommentMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sns_comment_id", nullable = false)
    private String snsCommentId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "content")
    private String content;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "crawled_at", nullable = false)
    private LocalDateTime crawledAt;

    @Builder
    public SnsPostCommentMetric(Long id, String snsCommentId, Long postId, String authorName, String content, Long likeCount, LocalDateTime publishedAt, LocalDateTime crawledAt) {
        this.id = id;
        this.snsCommentId = snsCommentId;
        this.postId = postId;
        this.authorName = authorName;
        this.content = content;
        this.likeCount = likeCount;
        this.publishedAt = publishedAt;
        this.crawledAt = crawledAt;
    }
}
