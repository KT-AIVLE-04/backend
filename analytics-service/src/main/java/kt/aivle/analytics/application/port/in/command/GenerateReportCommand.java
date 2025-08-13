package kt.aivle.analytics.application.port.in.command;

import java.time.LocalDate;

public record GenerateReportCommand(
    String userId,
    LocalDate startDate,
    LocalDate endDate
) {}
