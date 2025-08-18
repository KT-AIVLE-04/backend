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
public class SnsAccountMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "sns_account_id")
    private String snsAccountId;

    @Column(name = "followers")
    private Long followers;

    @Column(name = "views")
    private Long views;

    @Column(name = "crawled_at", nullable = false)
    private LocalDateTime crawledAt;

    @Builder
    public SnsAccountMetric(Long id, Long accountId, String snsAccountId, Long followers, Long views, LocalDateTime crawledAt) {
        this.id = id;
        this.accountId = accountId;
        this.snsAccountId = snsAccountId;
        this.followers = followers;
        this.views = views;
        this.crawledAt = crawledAt;
    }
}
