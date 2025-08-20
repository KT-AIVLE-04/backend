package kt.aivle.sns.application.port.in;

import kt.aivle.sns.adapter.in.web.dto.OAuthContext;
import kt.aivle.sns.domain.model.SnsType;

public interface SnsOAuthUseCase {
    SnsType supportSnsType();

    /**
     * 인증 URL 생성
     */
    String getAuthUrl(Long userId, Long storeId);

    /**
     * callback code 처리 및 토큰 저장
     */
    OAuthContext handleCallback(String state, String code) throws Exception;
}
