package kt.aivle.shortsmvc.dto;

public record JobStatusResponse(
    String jobId,
    String status,
    Integer progress,
    String downloadUrl,
    String resultKey,
    String error
) {}