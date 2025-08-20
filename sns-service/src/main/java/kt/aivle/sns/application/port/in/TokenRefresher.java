package kt.aivle.sns.application.port.in;

import kt.aivle.sns.domain.model.RefreshedToken;
import kt.aivle.sns.domain.model.SnsType;

public interface TokenRefresher {
    boolean supports(SnsType type);

    RefreshedToken refresh(String clientId, String clientSecret, String refreshToken);
}
