package kt.aivle.sns.application.port.in;

import kt.aivle.sns.domain.model.SnsType;

public interface SnsOAuthUseCase {
    SnsType supportSnsType();

    /**
     * 인증 URL 생성
     */
    String getAuthUrl(Long userId);

    /**
     * callback code 처리 및 토큰 저장
     */
    void handleCallback(Long userId, String code) throws Exception;
}
