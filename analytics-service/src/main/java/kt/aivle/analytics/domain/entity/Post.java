package kt.aivle.analytics.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "POST")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    
    @Column(name = "sns_post_id", nullable = false)
    private String snsPostId;
    
    public Post(Long accountId, String snsPostId) {
        this.accountId = accountId;
        this.snsPostId = snsPostId;
    }
}
