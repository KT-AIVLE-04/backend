package kt.aivle.auth.application.port.in.command;

import java.util.Optional;

import lombok.Builder;

@Builder
public record RefreshCommand(Optional<String> accessToken, String refreshToken) {
}
