package kt.aivle.content.event;

public record CreateContentRequestMessage(
        String requestId,
        Long userId,
        Long storeId,
        String key
) {
}