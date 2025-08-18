package kt.aivle.content.dto.request;

public record DeleteContentRequest(
        Long userId,
        Long storeId,
        Long id
) {
}