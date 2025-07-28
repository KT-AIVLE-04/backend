package kt.aivle.auth.application.service;

import static kt.aivle.auth.exception.AuthErrorCode.NOT_FOUND_USER;
import static kt.aivle.auth.exception.AuthErrorCode.UNAUTHORIZED_EMAIL;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.adapter.out.oauth2.dto.GoogleUserInfo;
import kt.aivle.auth.application.port.out.OAuth2Port;
import kt.aivle.auth.application.port.out.RefreshTokenRepositoryPort;
import kt.aivle.auth.application.port.out.UserRepositoryPort;
import kt.aivle.auth.domain.model.User;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.common.jwt.JwtDto;
import kt.aivle.common.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepositoryPort userRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final OAuth2Port oAuth2Port;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private static final long REFRESH_TOKEN_EXPIRE_MS = TimeUnit.DAYS.toMillis(14);

    /**
     * OAuth2 인증 URL 생성
     */
    public String generateAuthUrl() {
        return oAuth2Port.generateAuthUrl();
    }

    /**
     * OAuth2 콜백 처리
     */
    @Transactional
    public AuthResponse processOAuth2Callback(String code) {
        // 1. Authorization Code를 Access Token으로 교환
        String accessToken = oAuth2Port.exchangeCodeForToken(code);
        
        // 2. Access Token으로 사용자 정보 조회
        GoogleUserInfo userInfo = oAuth2Port.getUserInfo(accessToken);
        
        // 3. 사용자 처리 및 JWT 발급
        return processOAuth2User("google", userInfo.getSub(), userInfo.getEmail(), userInfo.getName());
    }

    @Transactional
    public AuthResponse processOAuth2User(String provider, String providerId, String email, String name) {
        log.info("Processing OAuth2 user: provider={}, email={}", provider, email);
        
        // 1. 기존 OAuth 사용자 확인
        Optional<User> existingOAuthUser = userRepositoryPort.findByProviderAndProviderId(provider, providerId);
        if (existingOAuthUser.isPresent()) {
            return generateAuthResponse(existingOAuthUser.get().getId());
        }
        
        // 2. 같은 이메일의 일반 회원 확인
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
                    .loginFailCount(user.getLoginFailCount())
                    .locked(user.isLocked())
                    .build();
            
            User savedUser = userRepositoryPort.save(updatedUser);
            return generateAuthResponse(savedUser.getId());
        }
        
        // 3. 새 OAuth 사용자 생성
        User newUser = User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .name(name)
                .password(passwordEncoder.encode("oauth_user_" + System.currentTimeMillis()))
                .build();
        
        User savedUser = userRepositoryPort.save(newUser);
        return generateAuthResponse(savedUser.getId());
    }

    private AuthResponse generateAuthResponse(Long userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_USER));

        if (user.isLocked()) {
            throw new BusinessException(UNAUTHORIZED_EMAIL);
        }

        // common 모듈의 JwtUtils 사용
        JwtDto jwt = jwtUtils.generateAccessToken(userId, user.getEmail());

        String refreshToken = UUID.randomUUID().toString();
        refreshTokenRepositoryPort.save(userId, refreshToken, REFRESH_TOKEN_EXPIRE_MS);

        return AuthResponse.builder()
                .type("Bearer")
                .accessToken(jwt.accessToken())
                .accessTokenExpiration(jwt.accessTokenExpiration())
                .refreshToken(refreshToken)
                .refreshTokenExpiration(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_MS)
                .build();
    }
} 