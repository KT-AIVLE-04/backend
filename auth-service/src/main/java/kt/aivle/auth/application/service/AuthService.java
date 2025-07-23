package kt.aivle.auth.application.service;

import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.application.port.in.AuthUseCase;
import kt.aivle.auth.application.port.in.command.LoginCommand;
import kt.aivle.auth.application.port.in.command.SignUpCommand;
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

import static kt.aivle.auth.exception.AuthErrorCode.*;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserLoginFailService userLoginFailService;
    private final UserPasswordPolicy passwordPolicyService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

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

        return generateAuthResponse(savedUser);
    }

    @Transactional
    @Override
    public AuthResponse login(LoginCommand command) {
        // 1. 이메일로 사용자 조회
        User user = userRepositoryPort.findByEmail(command.email());

        // 2. 계정이 잠금 되어 있는 경우
        if (user.isLocked()) {
            throw new BusinessException(UNAUTHORIZED_EMAIL); // 계정이 잠긴 경우
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            userLoginFailService.increaseFailCount(user);
            userRepositoryPort.save(user);
            throw new BusinessException(NOT_MATCHES_PASSWORD);
        }

        // 4. 로그인 성공 시 실패 횟수 초기화
        user.resetLoginFailCount();

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        JwtDto jwt = jwtUtils.generateAccessToken(user.getId(), user.getEmail());
        return AuthResponse.builder()
                .accessToken(jwt.accessToken())
                .accessTokenExpiration(jwt.accessTokenExpiration())
                .build();
    }
}
