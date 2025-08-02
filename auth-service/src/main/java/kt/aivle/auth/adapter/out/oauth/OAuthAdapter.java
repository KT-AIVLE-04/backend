package kt.aivle.auth.adapter.out.oauth;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import kt.aivle.auth.application.port.out.OAuthPort;
import kt.aivle.auth.domain.model.OAuthProvider;
import kt.aivle.auth.domain.model.OAuthUser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuthAdapter implements OAuthPort {

    /**
     * Spring Security OAuth2User를 도메인 OAuthUser로 변환
     */
    @Override
    public OAuthUser convertFromSpringOAuth2User(OAuth2User oauth2User, OAuthProvider provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String name = oauth2User.getName();

        return switch (provider) {
            case KAKAO -> convertFromKakaoAttributes(attributes, name);
            case GOOGLE -> convertFromGoogleAttributes(attributes, name);
        };
    }

    /**
     * 카카오 attributes를 OAuthUser로 변환
     */
    private OAuthUser convertFromKakaoAttributes(Map<String, Object> attributes, String name) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = kakaoAccount != null ? 
            (Map<String, Object>) kakaoAccount.get("profile") : null;
        
        String email = null;
        if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
            email = (String) kakaoAccount.get("email");
        }
        
        String displayName = null;
        if (profile != null && profile.containsKey("nickname")) {
            displayName = (String) profile.get("nickname");
        }
        
        String profileImageUrl = null;
        if (profile != null && profile.containsKey("profile_image_url")) {
            profileImageUrl = (String) profile.get("profile_image_url");
        }

        return OAuthUser.builder()
            .attributes(attributes)
            .name(name)
            .provider(OAuthProvider.KAKAO)
            .email(email)
            .displayName(displayName != null ? displayName : "카카오 사용자")
            .profileImageUrl(profileImageUrl)
            .phoneNumber(null)
            .token(null) // Spring Security가 토큰을 관리하므로 null
            .build();
    }

    /**
     * 구글 attributes를 OAuthUser로 변환
     */
    private OAuthUser convertFromGoogleAttributes(Map<String, Object> attributes, String name) {
        String email = (String) attributes.get("email");
        String displayName = (String) attributes.get("name");
        String profileImageUrl = (String) attributes.get("picture");

        return OAuthUser.builder()
            .attributes(attributes)
            .name(name)
            .provider(OAuthProvider.GOOGLE)
            .email(email)
            .displayName(displayName != null ? displayName : "구글 사용자")
            .profileImageUrl(profileImageUrl)
            .phoneNumber(null)
            .token(null) // Spring Security가 토큰을 관리하므로 null
            .build();
    }
} 