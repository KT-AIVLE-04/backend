package kt.aivle.auth.application.service;

import static kt.aivle.auth.exception.AuthErrorCode.INVALID_REDIRECT_URL;
import static kt.aivle.auth.exception.AuthErrorCode.UNAUTHORIZED_EMAIL;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.adapter.out.oauth2.dto.OAuth2UserInfo;
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
    
    private final OAuth2Port<OAuth2UserInfo> oAuth2Port;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final JwtUtils jwtUtils;

    private static final long REFRESH_TOKEN_EXPIRE_MS = TimeUnit.DAYS.toMillis(14);
    
    /**
     * OAuth2 인증 URL 생성
     */
    public String generateAuthUrl(String provider) {
        return oAuth2Port.generateAuthUrl(provider);
    }
    
    /**
     * OAuth2 토큰 교환 및 사용자 정보 조회
     */
    @Transactional
    public AuthResponse processOAuth2(String provider, String code) {
        try {
            // 1. 어댑터를 통해 토큰 교환 및 사용자 정보 조회
            String accessToken = oAuth2Port.exchangeCodeForToken(provider, code);
            OAuth2UserInfo userInfo = oAuth2Port.getUserInfo(provider, accessToken);
            
            // 2. 사용자 정보 검증
            if (!validateUserInfo(userInfo)) {
                throw new BusinessException(INVALID_REDIRECT_URL);
            }
            
            // 3. 사용자 조회 또는 생성
            User user = findOrCreateUser(userInfo);
            
            // 4. JWT 토큰 생성 및 반환
            return generateAuthResponse(user);
            
        } catch (Exception e) {
            log.error("{} OAuth2 처리 중 오류 발생", provider, e);
            throw new RuntimeException(provider + " OAuth2 처리 실패", e);
        }
    }
    
    private User findOrCreateUser(OAuth2UserInfo userInfo) {
        // 기존 OAuth 사용자 확인
        Optional<User> existingUser = userRepositoryPort.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId());
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // 같은 이메일의 일반 회원 확인
        Optional<User> emailUser = userRepositoryPort.findByEmailAndProviderIsNull(userInfo.getEmail());
        
        if (emailUser.isPresent()) {
            // 기존 회원을 OAuth 회원으로 연동
            User user = emailUser.get();
            User updatedUser = User.builder()
                    .provider(userInfo.getProvider())
                    .providerId(userInfo.getProviderId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .password(user.getPassword())
                    .phoneNumber(user.getPhoneNumber())
                    .build();
            
            return userRepositoryPort.save(updatedUser);
        }
        
        // 새 OAuth 사용자 생성
        User newUser = User.builder()
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .password(passwordEncoder.encode("oauth_user_" + System.currentTimeMillis()))
                .build();
        
        return userRepositoryPort.save(newUser);
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
    
    /**
     * OAuth2 사용자 정보 검증
     */
    private boolean validateUserInfo(OAuth2UserInfo userInfo) {
        if (userInfo == null) {
            return false;
        }
        
        // 필수 필드 검증
        boolean isValid = userInfo.getProviderId() != null && !userInfo.getProviderId().isEmpty();
        
        // 이메일 정보가 있는 경우 검증
        if (userInfo.getEmail() != null) {
            isValid = isValid && !userInfo.getEmail().isEmpty();
        }
        
        // 이름 정보가 있는 경우 검증
        if (userInfo.getName() != null) {
            isValid = isValid && !userInfo.getName().isEmpty();
        }
        
        log.info("{} 사용자 정보 검증 결과: {}", userInfo.getProvider(), isValid);
        return isValid;
    }
} 