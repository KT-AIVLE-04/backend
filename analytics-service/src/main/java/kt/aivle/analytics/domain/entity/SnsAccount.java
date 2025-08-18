package kt.aivle.analytics.domain.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import kt.aivle.analytics.domain.model.SnsType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class SnsAccount extends BaseEntity {

    @Id
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sns_account_id", nullable = false)
    private String snsAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SnsType type;

    @Builder
    public SnsAccount(Long id, Long userId, String snsAccountId, SnsType type) {
        this.id = id;
        this.userId = userId;
        this.snsAccountId = snsAccountId;
        this.type = type;
    }
}
