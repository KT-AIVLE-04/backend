package kt.aivle.sns.adapter.in.web.dto.request;

import java.time.LocalDateTime;

public record PostUpdateRequest(
        String snsType,
        String title,
        String description,
        String[] tags,
        boolean isNow,
        LocalDateTime publishAt
) {
}
