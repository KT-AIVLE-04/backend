package kt.aivle.content.dto;

import java.time.LocalDateTime;

public record ContentResponse(
        Long id,
        String title,
        String objectKey,
        String contentType,
        Integer width,
        Integer height,
        Integer durationSeconds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}