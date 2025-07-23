package kt.aivle.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import kt.aivle.auth.application.port.in.command.LoginCommand;

public record LoginRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
    public LoginCommand toCommand() {
        return LoginCommand.builder()
                .email(this.email)
                .password(this.password)
                .build();
    }
}
