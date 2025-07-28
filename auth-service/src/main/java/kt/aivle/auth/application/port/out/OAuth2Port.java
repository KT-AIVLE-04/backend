package kt.aivle.auth.application.port.out;

import kt.aivle.auth.adapter.out.oauth2.dto.GoogleUserInfo;

public interface OAuth2Port {
    
    /**
     * Authorization Code를 Access Token으로 교환
     */
    String exchangeCodeForToken(String code);
    
    /**
     * Access Token으로 사용자 정보 조회
     */
    GoogleUserInfo getUserInfo(String accessToken);
    
    /**
     * OAuth2 인증 URL 생성
     */
    String generateAuthUrl();
} 