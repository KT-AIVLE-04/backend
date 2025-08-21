package kt.aivle.sns.adapter.in.web.dto.request;

import java.util.List;

public record AiTagCreateRequest(
        String title,
        String description,
        List<String> keywords,
        String snsType,
        String industry,
        String location
) {
}