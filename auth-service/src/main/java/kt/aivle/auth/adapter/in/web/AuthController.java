package kt.aivle.auth.adapter.in.web;

import jakarta.validation.Valid;
import kt.aivle.auth.adapter.in.web.dto.LoginRequest;
import kt.aivle.auth.adapter.in.web.dto.SignUpRequest;
import kt.aivle.auth.application.port.in.AuthUseCase;
import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static kt.aivle.common.code.CommonResponseCode.CREATED;
import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final ResponseUtils responseUtils;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        authUseCase.singUp(request.toCommand());
        return responseUtils.build(CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request) {
        authUseCase.login(request.toCommand());
        return responseUtils.build(OK);
    }
}