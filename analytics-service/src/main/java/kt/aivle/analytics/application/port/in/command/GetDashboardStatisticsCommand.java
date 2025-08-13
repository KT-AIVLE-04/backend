package kt.aivle.analytics.application.port.in.command;

import java.time.LocalDate;

public record GetDashboardStatisticsCommand(
    String userId,
    LocalDate startDate,
    LocalDate endDate
) {}
