package kt.aivle.auth.application.port.out;

import org.springframework.security.oauth2.core.user.OAuth2User;

import kt.aivle.auth.domain.model.OAuthProvider;
import kt.aivle.auth.domain.model.OAuthUser;

public interface OAuthPort {
    /**
     * Spring Security OAuth2User를 도메인 OAuthUser로 변환
     */
    OAuthUser convertFromSpringOAuth2User(OAuth2User oauth2User, OAuthProvider provider);
} 