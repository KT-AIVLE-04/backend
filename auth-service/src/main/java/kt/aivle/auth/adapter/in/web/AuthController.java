package kt.aivle.auth.adapter.in.web;

import jakarta.validation.Valid;
import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.adapter.in.web.dto.LoginRequest;
import kt.aivle.auth.adapter.in.web.dto.SignUpRequest;
import kt.aivle.auth.application.port.in.AuthUseCase;
import kt.aivle.auth.application.port.in.command.LogoutCommand;
import kt.aivle.auth.application.port.in.command.RefreshCommand;
import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static kt.aivle.common.code.CommonResponseCode.CREATED;
import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
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

    @GetMapping("/{provider}/login")
    public ResponseEntity<ApiResponse<Void>> oauthLogin(@PathVariable String provider) {
        // OAuth 인증 플로우 시작을 위해 리다이렉트
        String oauthUrl = "/api/auth/oauth2/authorization/" + provider;
        return ResponseEntity.status(302)
                .header("Location", oauthUrl)
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {

        Optional<String> optionalAccessToken = Optional.ofNullable(accessToken)
                .map(token -> token.replace("Bearer ", ""));
        AuthResponse response = authUseCase.refresh(new RefreshCommand(optionalAccessToken, refreshToken));
        return responseUtils.build(OK, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        accessToken = accessToken.replace("Bearer ", "");
        authUseCase.logout(new LogoutCommand(accessToken, refreshToken));
        return responseUtils.build(OK, null);
    }
}