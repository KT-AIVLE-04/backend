package kt.aivle.analytics.domain.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class SnsPost extends BaseEntity {

    @Id
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "sns_post_id", nullable = false)
    private String snsPostId;

    @Builder
    public SnsPost(Long id, Long accountId, String snsPostId) {
        this.id = id;
        this.accountId = accountId;
        this.snsPostId = snsPostId;
    }
}
