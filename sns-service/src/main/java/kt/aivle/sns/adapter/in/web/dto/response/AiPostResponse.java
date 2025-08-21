package kt.aivle.sns.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiPostResponse(
        String title,
        String description,
        List<String> tags
) {

    public record AiSnakeCaseResponse(
            String title,
            @JsonProperty("content") String description,
            @JsonProperty("hashtags") List<String> tags
    ) {
    }

    public static AiPostResponse from(AiSnakeCaseResponse snakeCaseResponse) {
        return new AiPostResponse(
                snakeCaseResponse.title,
                snakeCaseResponse.description,
                snakeCaseResponse.tags
        );
    }
}