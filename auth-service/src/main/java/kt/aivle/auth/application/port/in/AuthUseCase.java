package kt.aivle.auth.application.port.in;

import kt.aivle.auth.application.port.in.command.LoginCommand;
import kt.aivle.auth.application.port.in.command.SignUpCommand;

public interface AuthUseCase {
    void singUp(SignUpCommand command);

    void login(LoginCommand command);
}
