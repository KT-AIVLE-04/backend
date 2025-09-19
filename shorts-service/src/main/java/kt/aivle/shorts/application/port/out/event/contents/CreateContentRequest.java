package kt.aivle.shorts.application.port.out.event.contents;

public record CreateContentRequest(
        String requestId,
        Long userId,
        Long storeId,
        String key
) {
}