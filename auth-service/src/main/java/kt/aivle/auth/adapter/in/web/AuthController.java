package kt.aivle.auth.adapter.in.web;

import static kt.aivle.common.code.CommonResponseCode.CREATED;
import static kt.aivle.common.code.CommonResponseCode.OK;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.adapter.in.web.dto.LoginRequest;
import kt.aivle.auth.adapter.in.web.dto.SignUpRequest;
import kt.aivle.auth.application.port.in.AuthUseCase;
import kt.aivle.auth.application.port.in.command.TokenCommand;
import kt.aivle.auth.application.service.OAuth2Service;
import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final OAuth2Service oAuth2Service;
    private final ResponseUtils responseUtils;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        AuthResponse response = authUseCase.signUp(request.toCommand());
        return responseUtils.build(CREATED, response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authUseCase.login(request.toCommand());
        return responseUtils.build(OK, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        accessToken = accessToken.replace("Bearer ", "");
        AuthResponse response = authUseCase.refresh(new TokenCommand(accessToken, refreshToken));
        return responseUtils.build(OK, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        accessToken = accessToken.replace("Bearer ", "");
        authUseCase.logout(new TokenCommand(accessToken, refreshToken));
        return responseUtils.build(OK, null);
    }

    @GetMapping("/oauth2/google")
    public ResponseEntity<ApiResponse<String>> googleLogin() {
        String googleAuthUrl = oAuth2Service.generateAuthUrl();
        return responseUtils.build(OK, googleAuthUrl);
    }

    @GetMapping("/oauth2/callback")
    public void oauthCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response
    ) throws Exception {
        try {
            // OAuth2Service를 통해 OAuth2 처리 및 JWT 발급
            AuthResponse authResponse = oAuth2Service.processOAuth2Callback(code);
            
            // 토큰과 함께 리다이렉트 url 생성
            String redirectUrl = String.format(
                "http://localhost:3000/auth/success?accessToken=%s&refreshToken=%s&expiresIn=%d",
                java.net.URLEncoder.encode(authResponse.accessToken(), java.nio.charset.StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(authResponse.refreshToken(), java.nio.charset.StandardCharsets.UTF_8),
                authResponse.accessTokenExpiration()
            );
            
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            log.error("Google OAuth 처리 중 오류 발생", e);
            response.sendRedirect("http://localhost:3000/auth/error?message=" + 
                java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}