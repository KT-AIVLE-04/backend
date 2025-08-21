package kt.aivle.sns.adapter.in.web.dto.request;

import java.util.List;

public record AiPostCreateRequest(
        String originalName,
        String objectKey,
        List<String> keywords,
        String snsType,
        String industry,
        String location
) {
}