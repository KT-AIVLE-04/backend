package kt.aivle.analytics.domain.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "post_keyword")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PostKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Builder
    public PostKeyword(Long id, Long postId, String keyword) {
        this.id = id;
        this.postId = postId;
        this.keyword = keyword;
    }
}
