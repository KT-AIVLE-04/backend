package kt.aivle.content.dto.response;

import java.time.LocalDateTime;

public record ContentResponse(
        Long id,
        String url,
        String title,
        String objectKey,
        String contentType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}