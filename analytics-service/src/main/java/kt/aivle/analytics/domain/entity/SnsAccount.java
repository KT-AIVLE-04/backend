package kt.aivle.analytics.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SNS_ACCOUNT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SnsAccount extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "sns_account_id", nullable = false)
    private String snsAccountId;
    
    @Column(name = "type", nullable = false)
    private String type;
    
    public SnsAccount(Long userId, String snsAccountId, String type) {
        this.userId = userId;
        this.snsAccountId = snsAccountId;
        this.type = type;
    }
}
