package kt.aivle.sns.application.port.in;

import kt.aivle.sns.domain.model.SnsToken;
import kt.aivle.sns.domain.model.SnsType;

public interface TokenServiceUseCase {
    SnsType supportSnsType();

    void saveToken(Long userId,
                   Long storeId,
                   String accessToken,
                   String refreshToken,
                   Long expiresInSeconds);

    SnsToken getTokenOrThrow(Long userId, Long storeId);

    SnsToken getTokenByAccountOrThrow(String accountId);

    /** 만료 임박 시 자동 리프레시하여 유효 토큰을 보장 */
    SnsToken ensureValidToken(Long userId, Long storeId);

    /** 명시적 리프레시: 기존 SnsToken을 갱신하여 반환 */
    SnsToken refresh(SnsToken token);
}
