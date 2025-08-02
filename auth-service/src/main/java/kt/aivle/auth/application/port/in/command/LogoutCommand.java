package kt.aivle.auth.application.port.in.command;

import lombok.Builder;

@Builder
public record LogoutCommand(String accessToken, String refreshToken) {
}
