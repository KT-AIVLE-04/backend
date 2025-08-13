package kt.aivle.analytics.application.port.in.command;

public record GetTopContentCommand(
    String userId,
    int limit
) {}
