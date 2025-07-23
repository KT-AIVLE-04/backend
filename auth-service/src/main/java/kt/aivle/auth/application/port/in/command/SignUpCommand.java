package kt.aivle.auth.application.port.in.command;

import lombok.Builder;

@Builder
public record SignUpCommand(String email, String password, String name, String phoneNumber) {
}
