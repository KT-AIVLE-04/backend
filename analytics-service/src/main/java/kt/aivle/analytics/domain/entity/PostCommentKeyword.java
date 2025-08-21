package kt.aivle.analytics.domain.entity;

import static lombok.AccessLevel.PROTECTED;

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

@Entity(name = "post_comment_keyword")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PostCommentKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "keyword", nullable = false)
    private String keyword;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", nullable = false)
    private SentimentType sentiment;

    @Builder
    public PostCommentKeyword(Long postId, String keyword, SentimentType sentiment) {
        this.postId = postId;
        this.keyword = keyword;
        this.sentiment = sentiment;
    }
}
