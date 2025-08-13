package kt.aivle.analytics.application.port.in.command;

import java.time.LocalDate;

public record AnalyzeTrendsCommand(
    String userId,
    LocalDate startDate,
    LocalDate endDate
) {}
