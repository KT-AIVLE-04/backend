package kt.aivle.analytics.application.port.in.command;

public record RefreshTokenCommand(
    String userId,
    String snsType
) {}
