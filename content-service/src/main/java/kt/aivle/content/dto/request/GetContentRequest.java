package kt.aivle.content.dto.request;

public record GetContentRequest(
        Long id,
        Long userId,
        Long storeId
) {
}