package kt.aivle.analytics.application.port.in.command;

import java.time.LocalDate;

public record GetPostMetricsCommand(
    String userId,
    Long socialPostId,
    LocalDate startDate,
    LocalDate endDate
) {}
