package kt.aivle.auth.application.service;

import static kt.aivle.auth.exception.AuthErrorCode.NOT_FOUND_USER;
import static kt.aivle.auth.exception.AuthErrorCode.OAUTH_LOGIN_FAILED;
import static kt.aivle.auth.exception.AuthErrorCode.UNAUTHORIZED_EMAIL;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.adapter.out.jwt.JwtDto;
import kt.aivle.auth.adapter.out.jwt.JwtUtils;
import kt.aivle.auth.application.port.in.OAuthUseCase;
import kt.aivle.auth.application.port.out.OAuthPort;
import kt.aivle.auth.application.port.out.RefreshTokenRepositoryPort;
import kt.aivle.auth.application.port.out.TokenBlacklistRepositoryPort;
import kt.aivle.auth.application.port.out.UserRepositoryPort;
import kt.aivle.auth.domain.model.OAuthProvider;
import kt.aivle.auth.domain.model.OAuthUser;
import kt.aivle.auth.domain.model.User;
import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService implements OAuthUseCase {

    private final OAuthPort oAuthPort;
    private final UserRepositoryPort userRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;
    private final JwtUtils jwtUtils;

    private static final long REFRESH_TOKEN_EXPIRE_MS = TimeUnit.DAYS.toMillis(14);

    /**
     * OAuth2 라이브러리를 활용한 로그인
     */
    @Override
    @Transactional
    public AuthResponse oauthLogin(OAuth2User oauth2User, OAuthProvider provider) {
        log.info("OAuth2 라이브러리 로그인 시작: provider={}", provider);
        
        try {
            // 1. Spring Security OAuth2User를 도메인 OAuthUser로 변환 (포트 사용)
            OAuthUser oauthUser = oAuthPort.convertFromSpringOAuth2User(oauth2User, provider);
            
            // 2. 도메인 로직으로 사용자 처리
            User user = processOAuthUser(oauthUser);
            
            // 3. JWT 토큰 생성
            AuthResponse authResponse = generateAuthResponse(user.getId());
            
            log.info("OAuth2 라이브러리 로그인 완료: userId={}, provider={}", user.getId(), provider);
            
            return authResponse;
            
        } catch (Exception e) {
            log.error("OAuth2 라이브러리 로그인 실패: provider={}, error={}", provider, e.getMessage(), e);
            throw new BusinessException(OAUTH_LOGIN_FAILED);
        }
    }

    /**
     * OAuth 사용자 정보로 회원 연결/가입 처리
     * 1. 기존사용자 정보있으면 조회하고 연결
     * 2. 기존사용자 중에 같은 이메일 있으면 조회하고 연결
     * 3. 기존사용자 중에 같은 이메일 없으면 새로 생성
     */
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
                existingUser.setProvider(oauthUser.getProvider().name());
                existingUser.setProviderId(oauthUser.getName());
                
                return userRepositoryPort.save(existingUser);
            }
        }
        
        // 3. 새 OAuth 사용자 생성
        log.info("새 OAuth 사용자 가입: provider={}, email={}", 
            oauthUser.getProvider(), oauthUser.getEmail());
        return createNewOAuthUser(oauthUser);
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

    @Override
    @Transactional
    public void oauthLogout(String accessToken, String refreshToken) {
        log.info("OAuth 로그아웃 시작");
        
        try {
            // 1. JWT 토큰 블랙리스트 처리
            io.jsonwebtoken.Claims claims = jwtUtils.parseClaimsAllowExpired(accessToken);
            blacklistIfNotExpired(claims);
            
            // 2. Refresh 토큰 삭제
            refreshTokenRepositoryPort.delete(refreshToken);
            
            // 3. OAuth 제공자별 토큰 무효화 (필요시)
            // Spring Security OAuth2는 자동으로 토큰을 관리하므로 별도 처리 불필요
            
            log.info("OAuth 로그아웃 완료");
            
        } catch (Exception e) {
            log.warn("OAuth 로그아웃 시 오류 발생: {}", e.getMessage());
            // 로그아웃 실패해도 Refresh 토큰은 삭제
            refreshTokenRepositoryPort.delete(refreshToken);
        }
    }

    /**
     * OAuth 로그인 후 쿠키 설정
     */
    public void setOAuthCookies(AuthResponse authResponse, HttpServletResponse response) {
        // Access Token 쿠키 설정
        Cookie accessTokenCookie = new Cookie("accessToken", authResponse.accessToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(false); // JavaScript에서 접근 가능
        response.addCookie(accessTokenCookie);
        
        // Refresh Token 쿠키 설정 (있는 경우에만)
        if (authResponse.refreshToken() != null) {
            Cookie refreshTokenCookie = new Cookie("refreshToken", authResponse.refreshToken());
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(false);
            response.addCookie(refreshTokenCookie);
        }
        
        log.info("OAuth 쿠키 설정 완료");
    }

    /**
     * JWT 토큰 생성
     */
    private AuthResponse generateAuthResponse(Long userId) {
        User user = userRepositoryPort.findById(userId)
            .orElseThrow(() -> new BusinessException(NOT_FOUND_USER));

        if (user.isLocked()) {
            throw new BusinessException(UNAUTHORIZED_EMAIL);
        }

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

    /**
     * 토큰 블랙리스트 처리
     */
    private void blacklistIfNotExpired(io.jsonwebtoken.Claims claims) {
        if (!jwtUtils.isExpired(claims)) {
            String jti = jwtUtils.getJti(claims);
            long ttl = jwtUtils.getExpiration(claims) - System.currentTimeMillis();
            tokenBlacklistRepositoryPort.addAccessTokenToBlacklist(jti, Math.max(ttl, 0));
        }
    }
} 