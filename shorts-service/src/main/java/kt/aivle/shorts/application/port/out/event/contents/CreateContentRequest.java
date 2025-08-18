package kt.aivle.shorts.application.port.out.event.contents;

public record CreateContentRequest(
        Long userId,
        Long storeId,
        String key
) {
}