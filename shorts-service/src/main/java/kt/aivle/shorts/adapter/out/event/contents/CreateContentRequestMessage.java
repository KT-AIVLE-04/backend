package kt.aivle.shorts.adapter.out.event.contents;

public record CreateContentRequestMessage(
        Long userId,
        Long storeId,
        String key
) {
}