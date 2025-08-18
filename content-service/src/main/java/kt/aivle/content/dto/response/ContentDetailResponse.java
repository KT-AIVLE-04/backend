package kt.aivle.content.dto.response;

import java.time.LocalDateTime;

public record ContentDetailResponse(
        Long id,
        String url,
        String title,
        String objectKey,
        String contentType,
        Integer width,
        Integer height,
        Integer durationSeconds,
        Long bytes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}