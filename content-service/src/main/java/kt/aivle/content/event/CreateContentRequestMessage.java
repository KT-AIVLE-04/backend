package kt.aivle.content.event;

public record CreateContentRequestMessage(
        Long userId,
        Long storeId,
        String url
) {
}