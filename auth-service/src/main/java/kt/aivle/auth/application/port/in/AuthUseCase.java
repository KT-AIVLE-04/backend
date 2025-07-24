package kt.aivle.auth.application.port.in;

import kt.aivle.auth.adapter.in.web.dto.AuthResponse;
import kt.aivle.auth.application.port.in.command.LoginCommand;
import kt.aivle.auth.application.port.in.command.SignUpCommand;

public interface AuthUseCase {
    AuthResponse signUp(SignUpCommand command);

    AuthResponse login(LoginCommand command);

    AuthResponse refresh(String refreshToken);
}
