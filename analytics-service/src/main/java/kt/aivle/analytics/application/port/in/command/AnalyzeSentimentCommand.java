package kt.aivle.analytics.application.port.in.command;

public record AnalyzeSentimentCommand(
    String userId,
    String videoId
) {}
