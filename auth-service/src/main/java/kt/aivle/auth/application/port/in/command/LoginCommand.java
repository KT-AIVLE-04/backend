package kt.aivle.auth.application.port.in.command;

import lombok.Builder;

@Builder
public record LoginCommand(String email, String password) {
}
