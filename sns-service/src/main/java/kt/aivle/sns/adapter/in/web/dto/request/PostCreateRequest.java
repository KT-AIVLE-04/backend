package kt.aivle.sns.adapter.in.web.dto.request;

import java.time.LocalDateTime;

public record PostCreateRequest(
        String snsType,
        String originalName,
        String objectKey,
        String title,
        String description,
        String[] tags,
        boolean isNow,
        LocalDateTime publishAt
) {
}