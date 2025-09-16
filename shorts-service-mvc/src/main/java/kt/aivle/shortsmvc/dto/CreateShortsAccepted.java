package kt.aivle.shortsmvc.dto;

public record CreateShortsAccepted(
    String jobId,
    String status,
    String location
) {}