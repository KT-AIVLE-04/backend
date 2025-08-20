package kt.aivle.analytics.domain.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kt.aivle.analytics.domain.model.SentimentType;
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

    @Column(name = "author_id")
    private String authorId;

    @Column(name = "content")
    private String content;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", nullable = false)
    private SentimentType sentiment;

    @Builder
    public SnsPostCommentMetric(Long id, String snsCommentId, Long postId, String authorId, String content, Long likeCount, LocalDateTime publishedAt, SentimentType sentiment) {
        this.id = id;
        this.snsCommentId = snsCommentId;
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.likeCount = likeCount;
        this.publishedAt = publishedAt;
        this.sentiment = sentiment;
    }
}
