package kt.aivle.shorts.application.port.out.event.contents;

public record CreateContentRequest(
        Long userId,
        Long storeId,
        String url,
        String s3Key,
        String originalName,
        String contentType
) {
}