package kt.aivle.sns.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="oauth_state")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OAuthStateEntity {
    @Id
    private String state;
    private Long userId;
    private Long storeId;
    private Instant expiresAt;
}
