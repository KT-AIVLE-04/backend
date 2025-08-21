package kt.aivle.sns.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "oauth_state")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthStateEntity {
    @Id
    private String state;
    private Long userId;
    private Long storeId;
    private Instant expiresAt;

    @Builder
    public OAuthStateEntity(String state, Long userId, Long storeId, Instant expiresAt) {
        this.state = state;
        this.userId = userId;
        this.storeId = storeId;
        this.expiresAt = expiresAt;
    }
}
