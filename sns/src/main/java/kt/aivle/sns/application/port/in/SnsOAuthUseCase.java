package kt.aivle.sns.application.port.in;

import kt.aivle.sns.domain.model.SnsType;

public interface SnsOAuthUseCase {
    SnsType supportSnsType();

    /**
     * 인증 URL 생성
     */
    String getAuthUrl(String userId);

    /**
     * callback code 처리 및 토큰 저장
     */
    void handleCallback(String userId, String code) throws Exception;

    /**
     * access token 조회
     */
    String getAccessToken(String userId);

    /**
     * refresh token 조회
     */
    String getRefreshToken(String userId);
}
