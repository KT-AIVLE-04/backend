package kt.aivle.auth.application.port.in.command;

import java.util.Optional;


public record RefreshCommand(Optional<String> accessToken, String refreshToken) {
}
