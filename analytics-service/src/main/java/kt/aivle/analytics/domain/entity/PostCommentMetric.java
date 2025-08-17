package kt.aivle.analytics.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "POST_COMMENT_METRIC")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostCommentMetric extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sns_comment_id", nullable = false)
    private String snsCommentId;
    
    @Column(name = "post_id", nullable = false)
    private Long postId;
    
    @Column(name = "content")
    private String content;
    
    @Column(name = "crawled_at", nullable = false)
    private LocalDateTime crawledAt;
    
    public PostCommentMetric(String snsCommentId, Long postId, String content, LocalDateTime crawledAt) {
        this.snsCommentId = snsCommentId;
        this.postId = postId;
        this.content = content;
        this.crawledAt = crawledAt;
    }
}
