package kt.aivle.shorts.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record ScenarioResponse(String title, String content) {

    public static ScenarioResponse of(String title, String content) {
        return ScenarioResponse.builder()
                .title(title)
                .content(content)
                .build();
    }
}
