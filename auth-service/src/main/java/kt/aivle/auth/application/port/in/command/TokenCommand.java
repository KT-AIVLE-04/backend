package kt.aivle.auth.application.port.in.command;

import lombok.Builder;

@Builder
public record TokenCommand(String accessToken, String refreshToken) {
}
