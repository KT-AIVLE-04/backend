package kt.aivle.auth.application.port.out;

import org.springframework.security.oauth2.core.user.OAuth2User;

import kt.aivle.auth.domain.model.OAuthUser;
import kt.aivle.auth.domain.model.User;

public interface OAuthPort {
    /**
     * OAuth 사용자 정보로 회원 연결/가입 처리
     * 1. 기존사용자 정보있으면 조회하고 연결
     * 2. 기존사용자 중에 같은 이메일 있으면 조회하고 연결
     * 3. 기존사용자 중에 같은 이메일 없으면 새로 생성
     */
    User processOAuthUser(OAuthUser oauthUser);
    
    /**
     * Spring Security OAuth2User를 도메인 OAuthUser로 변환
     */
    OAuthUser convertFromSpringOAuth2User(OAuth2User oauth2User, kt.aivle.auth.domain.model.OAuthProvider provider);
} 