package kt.aivle.sns.adapter.in.web.dto.response;

import java.util.List;

public record AiTagResponse(
        List<String> tags
) {

    public record AiSnakeCaseResponse(
            List<String> hashtags
    ) {
    }

    public static AiTagResponse from(AiSnakeCaseResponse snakeCaseResponse) {
        return new AiTagResponse(snakeCaseResponse.hashtags);
    }
}