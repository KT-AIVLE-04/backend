package kt.aivle.auth.adapter.in.web;

import static kt.aivle.common.code.CommonResponseCode.CREATED;
import static kt.aivle.common.code.CommonResponseCode.OK;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.adapter.in.web.dto.LoginRequest;
import kt.aivle.auth.adapter.in.web.dto.SignUpRequest;
import kt.aivle.auth.application.port.in.AuthUseCase;
import kt.aivle.auth.application.port.in.command.TokenCommand;
import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final ResponseUtils responseUtils;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    public ResponseEntity<ApiResponse<AuthResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        AuthResponse response = authUseCase.signUp(request.toCommand());
        return responseUtils.build(CREATED, response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authUseCase.login(request.toCommand());
        return responseUtils.build(OK, response);
    }

    @GetMapping("/oauth2/{provider}")
    @Operation(summary = "OAuth2 로그인", description = "OAuth2 제공자로 로그인을 시작합니다")
    public ResponseEntity<Void> oauth2Login(
            @Parameter(description = "OAuth2 제공자 (google, github 등)", required = true)
            @PathVariable String provider,
            HttpServletResponse response) throws Exception {
        
        log.info("OAuth2 로그인 요청: {}", provider);
        
        // Spring Security의 OAuth2 엔드포인트로 리다이렉트
        String redirectUrl = "/api/oauth2/authorization/" + provider;
        response.sendRedirect(redirectUrl);
        
        return ResponseEntity.status(HttpStatus.FOUND).build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "액세스 토큰을 갱신합니다")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        accessToken = accessToken.replace("Bearer ", "");
        AuthResponse response = authUseCase.refresh(new TokenCommand(accessToken, refreshToken));
        return responseUtils.build(OK, response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃하고 토큰을 무효화합니다")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        accessToken = accessToken.replace("Bearer ", "");
        authUseCase.logout(new TokenCommand(accessToken, refreshToken));
        return responseUtils.build(OK, null);
    }
}