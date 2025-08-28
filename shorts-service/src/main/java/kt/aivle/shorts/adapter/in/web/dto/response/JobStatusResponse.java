package kt.aivle.shorts.adapter.in.web.dto.response;

public record JobStatusResponse(
        String jobId,
        String status,
        int progress,
        String videoUrl,
        String key,
        String error
) {
}