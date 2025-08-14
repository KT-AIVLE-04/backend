package kt.aivle.content.event;

public record CreateContentRequestMessage(
        Long userId,
        Long storeId,
        String url,
        String s3Key,
        String originalName,
        String contentType
) {
}