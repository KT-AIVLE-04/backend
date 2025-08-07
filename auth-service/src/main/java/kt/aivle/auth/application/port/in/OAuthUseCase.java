package kt.aivle.auth.application.port.in;

import org.springframework.security.oauth2.core.user.OAuth2User;

import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.domain.model.OAuthProvider;

public interface OAuthUseCase {
    /**
     * OAuth2 라이브러리를 활용한 로그인 처리
     */
    AuthResponse oauthLogin(OAuth2User oauth2User, OAuthProvider provider);
    
    /**
     * OAuth 로그아웃
     */
    void oauthLogout(String accessToken, String refreshToken);
} 