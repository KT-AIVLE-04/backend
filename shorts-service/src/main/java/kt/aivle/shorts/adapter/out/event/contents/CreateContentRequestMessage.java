package kt.aivle.shorts.adapter.out.event.contents;

public record CreateContentRequestMessage(
        String requestId,
        Long userId,
        Long storeId,
        String key
) {
}