package kt.aivle.shorts.adapter.out.event.contents;

public record CreateContentRequestMessage(
        Long userId,
        Long storeId,
        String url,
        String s3Key,
        String originalName,
        String contentType
) {
}