package kt.aivle.auth.application.service;

import static kt.aivle.auth.exception.AuthErrorCode.INVALID_REDIRECT_URL;
import static kt.aivle.auth.exception.AuthErrorCode.UNAUTHORIZED_EMAIL;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.application.port.out.RefreshTokenRepositoryPort;
import kt.aivle.auth.application.port.out.UserRepositoryPort;
import kt.aivle.auth.domain.model.OAuth2UserPrincipal;
import kt.aivle.auth.domain.model.User;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.common.jwt.JwtDto;
import kt.aivle.common.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final JwtUtils jwtUtils;

    private static final long REFRESH_TOKEN_EXPIRE_MS = TimeUnit.DAYS.toMillis(14);

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        
        log.info("OAuth2 Login: {}, attributes: {}", registrationId, oAuth2User.getAttributes());
        
        // Google OAuth2 사용자 정보 추출
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        
        // 기존 사용자 확인 또는 새 사용자 생성
        User user = findOrCreateUser(registrationId, providerId, email, name);
        
        return new OAuth2UserPrincipal(user, oAuth2User.getAttributes(), userNameAttributeName);
    }
    
    private User findOrCreateUser(String provider, String providerId, String email, String name) {
        // 기존 OAuth 사용자 확인
        Optional<User> existingUser = userRepositoryPort.findByProviderAndProviderId(provider, providerId);
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // 같은 이메일의 일반 회원 확인
        Optional<User> emailUser = userRepositoryPort.findByEmailAndProviderIsNull(email);
        
        if (emailUser.isPresent()) {
            // 기존 회원을 OAuth 회원으로 연동
            User user = emailUser.get();
            User updatedUser = User.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .email(user.getEmail())
                    .name(user.getName())
                    .password(user.getPassword())
                    .phoneNumber(user.getPhoneNumber())
                    .build();
            
            return userRepositoryPort.save(updatedUser);
        }
        
        // 새 OAuth 사용자 생성
        User newUser = User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .name(name)
                .password(passwordEncoder.encode("oauth_user_" + System.currentTimeMillis()))
                .build();
        
        return userRepositoryPort.save(newUser);
    }

    public void handleOAuth2Success(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws Exception {
        try {
            OAuth2UserPrincipal oauth2User = (OAuth2UserPrincipal) authentication.getPrincipal();
            User user = oauth2User.getUser();
            
            if (user.isLocked()) {
                throw new BusinessException(UNAUTHORIZED_EMAIL);
            }
            
            // JWT 토큰 생성
            AuthResponse authResponse = generateAuthResponse(user);
            
            // 요청에서 리다이렉트 URL 추출
            String redirectUrl = extractRedirectUrl(request);
            if (redirectUrl == null) {
                throw new BusinessException(INVALID_REDIRECT_URL);
            }
            
            // 토큰과 함께 리다이렉트 url 생성
            String finalRedirectUrl = String.format(
                "%s?accessToken=%s&refreshToken=%s&expiresIn=%d",
                redirectUrl,
                java.net.URLEncoder.encode(authResponse.accessToken(), java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(authResponse.refreshToken(), java.nio.charset.StandardCharsets.UTF_8),
                authResponse.accessTokenExpiration()
            );
            
            response.sendRedirect(finalRedirectUrl);
            
        } catch (Exception e) {
            log.error("OAuth2 성공 처리 중 오류 발생", e);
            String errorRedirectUrl = extractRedirectUrl(request);
            if (errorRedirectUrl == null) {
                throw new BusinessException(INVALID_REDIRECT_URL);
            }
            response.sendRedirect(errorRedirectUrl + "?message=" + 
                java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private AuthResponse generateAuthResponse(User user) {
        if (user.isLocked()) {
            throw new BusinessException(UNAUTHORIZED_EMAIL);
        }

        // JWT 토큰 생성
        JwtDto jwt = jwtUtils.generateAccessToken(user.getId(), user.getEmail());

        String refreshToken = UUID.randomUUID().toString();
        refreshTokenRepositoryPort.save(user.getId(), refreshToken, REFRESH_TOKEN_EXPIRE_MS);

        return AuthResponse.builder()
                .type("Bearer")
                .accessToken(jwt.accessToken())
                .accessTokenExpiration(jwt.accessTokenExpiration())
                .refreshToken(refreshToken)
                .refreshTokenExpiration(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_MS)
                .build();
    }

    private String extractRedirectUrl(HttpServletRequest request) {
        // Origin 헤더에서 프론트엔드 URL 추출
        String origin = request.getHeader("Origin");
        if (origin != null) {
            return origin + "/auth/success";
        }
        
        return null;
    }
} 