package kt.aivle.content.dto.request;

public record GetContentListRequest(
        Long userId,
        Long storeId,
        String query
) {
}