package kt.aivle.auth.adapter.out.oauth;

import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import kt.aivle.auth.adapter.out.oauth.dto.GoogleTokenResponse;
import kt.aivle.auth.adapter.out.oauth.dto.GoogleUserInfo;
import kt.aivle.auth.adapter.out.oauth.dto.KakaoTokenResponse;
import kt.aivle.auth.adapter.out.oauth.dto.KakaoUserInfo;
import kt.aivle.auth.application.port.out.OAuthPort;
import kt.aivle.auth.application.port.out.UserRepositoryPort;
import kt.aivle.auth.domain.model.OAuthProvider;
import kt.aivle.auth.domain.model.OAuthToken;
import kt.aivle.auth.domain.model.OAuthUserInfo;
import kt.aivle.auth.domain.model.User;
import kt.aivle.auth.properties.OAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthAdapter implements OAuthPort {
    
    private final RestTemplate restTemplate;
    private final OAuthProperties oAuthProperties;
    private final UserRepositoryPort userRepositoryPort;
    
    @Override
    public OAuthToken authenticate(String authorizationCode, OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> getKakaoAccessToken(authorizationCode);
            case GOOGLE -> getGoogleAccessToken(authorizationCode);
        };
    }
    
    @Override
    public OAuthUserInfo getUserInfo(String accessToken, OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> getKakaoUserInfo(accessToken);
            case GOOGLE -> getGoogleUserInfo(accessToken);
        };
    }
    
    @Override
    public User linkOrSignupUser(OAuthUserInfo oauthUserInfo) {
        // 1. provider + providerId로 기존 OAuth 회원 조회
        Optional<User> existingOAuthUser = userRepositoryPort.findByProviderAndProviderId(
            oauthUserInfo.getProvider(), oauthUserInfo.getProviderId()
        );
        
        if (existingOAuthUser.isPresent()) {
            log.info("기존 OAuth 회원 로그인: provider={}, providerId={}", 
                oauthUserInfo.getProvider(), oauthUserInfo.getProviderId());
            return existingOAuthUser.get();
        }
        
        // 2. 이메일로 기존 회원 조회 (OAuth 연결)
        Optional<User> existingUser = userRepositoryPort.findByEmail(oauthUserInfo.getEmail());
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // 기존 회원에 OAuth 정보 연결
            user = User.builder()
                .provider(oauthUserInfo.getProvider().name())
                .providerId(oauthUserInfo.getProviderId())
                .email(user.getEmail())
                .name(user.getName())
                .password(user.getPassword())
                .phoneNumber(user.getPhoneNumber())
                .loginFailCount(user.getLoginFailCount())
                .locked(user.isLocked())
                .build();
            
            log.info("기존 회원에 OAuth 연결: email={}, provider={}", 
                oauthUserInfo.getEmail(), oauthUserInfo.getProvider());
            return userRepositoryPort.save(user);
        }
        
        // 3. 신규 회원 생성
        User newUser = User.builder()
            .provider(oauthUserInfo.getProvider().name())
            .providerId(oauthUserInfo.getProviderId())
            .email(oauthUserInfo.getEmail())
            .name(oauthUserInfo.getName())
            .password(null) // OAuth 회원은 비밀번호 없음
            .phoneNumber(oauthUserInfo.getPhoneNumber())
            .loginFailCount(0)
            .locked(false)
            .build();
        
        log.info("신규 OAuth 회원 가입: email={}, provider={}", 
            oauthUserInfo.getEmail(), oauthUserInfo.getProvider());
        return userRepositoryPort.save(newUser);
    }
    
    private OAuthToken getKakaoAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", oAuthProperties.getKakao().getClientId());
        body.add("client_secret", oAuthProperties.getKakao().getClientSecret());
        body.add("code", authorizationCode);
        body.add("redirect_uri", oAuthProperties.getKakao().getRedirectUri());
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
            "https://kauth.kakao.com/oauth/token",
            HttpMethod.POST,
            request,
            KakaoTokenResponse.class
        );
        
        KakaoTokenResponse tokenResponse = response.getBody();
        
        return OAuthToken.builder()
            .accessToken(tokenResponse.getAccessToken())
            .refreshToken(tokenResponse.getRefreshToken())
            .tokenType(tokenResponse.getTokenType())
            .expiresIn(tokenResponse.getExpiresIn())
            .scope(tokenResponse.getScopeList())
            .refreshTokenExpiresIn(tokenResponse.getRefreshTokenExpiresIn())
            .build();
    }
    
    private OAuthToken getGoogleAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", oAuthProperties.getGoogle().getClientId());
        body.add("client_secret", oAuthProperties.getGoogle().getClientSecret());
        body.add("code", authorizationCode);
        body.add("redirect_uri", oAuthProperties.getGoogle().getRedirectUri());
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<GoogleTokenResponse> response = restTemplate.exchange(
            "https://oauth2.googleapis.com/token",
            HttpMethod.POST,
            request,
            GoogleTokenResponse.class
        );
        
        GoogleTokenResponse tokenResponse = response.getBody();
        
        return OAuthToken.builder()
            .accessToken(tokenResponse.getAccessToken())
            .refreshToken(tokenResponse.getRefreshToken())
            .tokenType(tokenResponse.getTokenType())
            .expiresIn(tokenResponse.getExpiresIn())
            .scope(tokenResponse.getScopeList())
            .refreshTokenExpiresIn(null) // 구글은 refresh_token_expires_in 필드가 없음
            .build();
    }
    
    private OAuthUserInfo getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.GET,
            request,
            KakaoUserInfo.class
        );
        
        KakaoUserInfo kakaoUserInfo = response.getBody();
        
        return OAuthUserInfo.builder()
            .providerId(kakaoUserInfo.getId().toString())
            .email(kakaoUserInfo.getKakaoAccount().getEmail())
            .name(kakaoUserInfo.getKakaoAccount().getProfile().getNickname())
            .profileImageUrl(kakaoUserInfo.getKakaoAccount().getProfile().getProfileImageUrl())
            .phoneNumber(kakaoUserInfo.getKakaoAccount().getPhoneNumber())
            .provider(OAuthProvider.KAKAO)
            .build();
    }
    
    private OAuthUserInfo getGoogleUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
            "https://www.googleapis.com/oauth2/v2/userinfo",
            HttpMethod.GET,
            request,
            GoogleUserInfo.class
        );
        
        GoogleUserInfo googleUserInfo = response.getBody();
        
        return OAuthUserInfo.builder()
            .providerId(googleUserInfo.getId())
            .email(googleUserInfo.getEmail())
            .name(googleUserInfo.getName())
            .profileImageUrl(googleUserInfo.getPicture())
            .phoneNumber(null) // 구글은 휴대폰번호 제공 안함
            .provider(OAuthProvider.GOOGLE)
            .build();
    }
} 