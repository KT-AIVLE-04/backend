package kt.aivle.sns.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshedToken {
    private final String accessToken;
    private final Long expiresAt;
    private final String refreshToken;  // 대개 null; 값 오면 교체
}
