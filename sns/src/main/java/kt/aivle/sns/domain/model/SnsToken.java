package kt.aivle.sns.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SnsToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    private String userId;

    @Enumerated(EnumType.STRING)
    private SnsType snsType;

    @Lob
    private String accessToken;

    @Lob
    private String refreshToken;

    private Long expiresAt;
}
