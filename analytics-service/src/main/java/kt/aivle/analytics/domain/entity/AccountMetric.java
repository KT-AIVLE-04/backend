package kt.aivle.analytics.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ACCOUNT_METRIC")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountMetric extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sns_account_id", nullable = false)
    private Long snsAccountId;
    
    @Column(name = "followers")
    private Long followers;
    
    @Column(name = "views")
    private Long views;
    
    @Column(name = "crawled_at", nullable = false)
    private LocalDateTime crawledAt;
    
    public AccountMetric(Long snsAccountId, Long followers, Long views, LocalDateTime crawledAt) {
        this.snsAccountId = snsAccountId;
        this.followers = followers;
        this.views = views;
        this.crawledAt = crawledAt;
    }
}
