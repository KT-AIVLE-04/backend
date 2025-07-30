package kt.aivle.auth.application.service;

import io.jsonwebtoken.Claims;
import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.application.port.in.AuthUseCase;
import kt.aivle.auth.application.port.in.command.LoginCommand;
import kt.aivle.auth.application.port.in.command.LogoutCommand;
import kt.aivle.auth.application.port.in.command.RefreshCommand;
import kt.aivle.auth.application.port.in.command.SignUpCommand;
import kt.aivle.auth.application.port.out.RefreshTokenRepositoryPort;
import kt.aivle.auth.application.port.out.TokenBlacklistRepositoryPort;
import kt.aivle.auth.application.port.out.UserRepositoryPort;
import kt.aivle.auth.domain.model.User;
import kt.aivle.auth.domain.service.UserPasswordPolicy;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.common.jwt.JwtDto;
import kt.aivle.common.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static kt.aivle.auth.exception.AuthErrorCode.*;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;
    private final UserLoginFailService userLoginFailService;
    private final UserPasswordPolicy passwordPolicyService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private static final long REFRESH_TOKEN_EXPIRE_MS = TimeUnit.DAYS.toMillis(14);

    @Transactional
    @Override
    public AuthResponse signUp(SignUpCommand command) {
        // 1. 이메일 중복 체크
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new BusinessException(DUPLICATE_EMAIL);
        }

        // 2. 비밀번호 복잡도/정책 검증
        if (!passwordPolicyService.isValid(command.email(), command.password())) {
            throw new BusinessException(INVALID_PASSWORD_POLICY);
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(command.password());

        // 4. User 생성
        User user = User.builder()
                .provider("local")
                .email(command.email())
                .password(encodedPassword)
                .name(command.name())
                .phoneNumber(command.phoneNumber())
                .build();

        User savedUser = userRepositoryPort.save(user);

        return generateAuthResponse(savedUser.getId());
    }

    @Transactional
    @Override
    public AuthResponse login(LoginCommand command) {
        // 1. 이메일로 사용자 조회
        User user = userRepositoryPort.findByEmail(command.email())
                .orElseThrow(() -> new BusinessException(NOT_FOUND_EMAIL));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            userLoginFailService.increaseFailCount(user);
            throw new BusinessException(NOT_MATCHES_PASSWORD);
        }

        // 3. 토큰 생성
        AuthResponse authResponse = generateAuthResponse(user.getId());

        // 4. 로그인 성공 시 실패 횟수 초기화
        user.resetLoginFailCount();

        return authResponse;
    }

    @Transactional
    @Override
    public AuthResponse refresh(RefreshCommand command) {
        // 1. refreshToken 토큰 검사
        Long userId = refreshTokenRepositoryPort.findUserIdByToken(command.refreshToken())
                .orElseThrow(() -> new BusinessException(INVALID_REFRESH_TOKEN));

        // 2. accessToken이 있으면 서명 검증만 + 만료 무시하고 블랙리스트에 추가
        command.accessToken().ifPresent(accessToken -> {
            Claims claims = jwtUtils.parseClaimsAllowExpired(accessToken);
            blacklistIfNotExpired(claims);
        });

        // 3. 토큰 생성
        AuthResponse authResponse = generateAuthResponse(userId);

        // 4. 기존 refreshToken 삭제
        refreshTokenRepositoryPort.delete(command.refreshToken());

        return authResponse;
    }

    @Transactional
    @Override
    public void logout(LogoutCommand command) {
        // 1. accessToken 서명 검증만 + 만료 무시하고 블랙리스트에 추가
        Claims claims = jwtUtils.parseClaimsAllowExpired(command.accessToken());
        blacklistIfNotExpired(claims);

        // 2. refreshToken 삭제
        refreshTokenRepositoryPort.delete(command.refreshToken());
    }

    private AuthResponse generateAuthResponse(Long userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_USER));

        if (user.isLocked()) {
            throw new BusinessException(UNAUTHORIZED_EMAIL);
        }

        JwtDto jwt = jwtUtils.generateAccessToken(userId, user.getEmail());

        String refreshToken = UUID.randomUUID().toString();
        refreshTokenRepositoryPort.save(userId, refreshToken, REFRESH_TOKEN_EXPIRE_MS); // 14일 만료

        return AuthResponse.builder()
                .type("Bearer")
                .accessToken(jwt.accessToken())
                .accessTokenExpiration(jwt.accessTokenExpiration())
                .refreshToken(refreshToken)
                .refreshTokenExpiration(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_MS)
                .build();
    }

    private void blacklistIfNotExpired(Claims claims) {
        if (!jwtUtils.isExpired(claims)) {
            String jti = jwtUtils.getJti(claims);
            long ttl = jwtUtils.getExpiration(claims) - System.currentTimeMillis();
            tokenBlacklistRepositoryPort.addAccessTokenToBlacklist(jti, Math.max(ttl, 0));
        }
    }
}
