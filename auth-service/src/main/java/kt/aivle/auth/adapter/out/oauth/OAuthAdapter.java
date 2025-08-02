package kt.aivle.auth.adapter.out.oauth;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import kt.aivle.auth.application.port.out.OAuthPort;
import kt.aivle.auth.application.port.out.UserRepositoryPort;
import kt.aivle.auth.domain.model.OAuthProvider;
import kt.aivle.auth.domain.model.OAuthUser;
import kt.aivle.auth.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthAdapter implements OAuthPort {

    private final UserRepositoryPort userRepositoryPort;

    /**
     * OAuth 사용자 정보로 회원 연결/가입 처리
     * 1. 기존사용자 정보있으면 조회하고 연결
     * 2. 기존사용자 중에 같은 이메일 있으면 조회하고 연결
     * 3. 기존사용자 중에 같은 이메일 없으면 새로 생성
     */
    @Override
    public User processOAuthUser(OAuthUser oauthUser) {
        log.info("OAuth 사용자 처리 시작: provider={}", oauthUser.getProvider());
        
        // 1. 기존 OAuth 사용자 조회 (provider + providerId)
        Optional<User> existingOAuthUser = userRepositoryPort.findByProviderAndProviderId(
            oauthUser.getProvider(), oauthUser.getName());
        
        if (existingOAuthUser.isPresent()) {
            log.info("기존 OAuth 사용자 로그인: userId={}, provider={}", 
                existingOAuthUser.get().getId(), oauthUser.getProvider());
            return existingOAuthUser.get();
        }
        
        // 2. 같은 이메일로 기존 회원 조회 (OAuth 연결)
        if (oauthUser.getEmail() != null) {
            Optional<User> existingUserByEmail = userRepositoryPort.findByEmail(oauthUser.getEmail());
            
            if (existingUserByEmail.isPresent()) {
                User existingUser = existingUserByEmail.get();
                log.info("기존 회원에 OAuth 연결: userId={}, email={}, provider={}", 
                    existingUser.getId(), oauthUser.getEmail(), oauthUser.getProvider());
                
                // 기존 회원에 OAuth 정보 연결
                User linkedUser = User.builder()
                    .provider(oauthUser.getProvider().name())
                    .providerId(oauthUser.getName())
                    .email(existingUser.getEmail())
                    .name(existingUser.getName())
                    .password(existingUser.getPassword())
                    .phoneNumber(existingUser.getPhoneNumber())
                    .loginFailCount(existingUser.getLoginFailCount())
                    .locked(existingUser.isLocked())
                    .build();
                
                return userRepositoryPort.save(linkedUser);
            }
        }
        
        // 3. 새 OAuth 사용자 생성
        log.info("새 OAuth 사용자 가입: provider={}, email={}", 
            oauthUser.getProvider(), oauthUser.getEmail());
        return createNewOAuthUser(oauthUser);
    }

    /**
     * Spring Security OAuth2User를 도메인 OAuthUser로 변환
     */
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

    /**
     * 새 OAuth 사용자 생성
     */
    private User createNewOAuthUser(OAuthUser oauthUser) {
        User newUser = User.builder()
            .provider(oauthUser.getProvider().name())
            .providerId(oauthUser.getName())
            .email(oauthUser.getEmail())
            .name(oauthUser.getDisplayName())
            .password(null) // OAuth 회원은 비밀번호 없음
            .phoneNumber(oauthUser.getPhoneNumber())
            .loginFailCount(0)
            .locked(false)
            .build();
        
        return userRepositoryPort.save(newUser);
    }
} 