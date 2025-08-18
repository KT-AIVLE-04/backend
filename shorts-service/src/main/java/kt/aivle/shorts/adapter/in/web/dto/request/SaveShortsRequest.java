package kt.aivle.shorts.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SaveShortsRequest(
        @NotBlank(message = "key를 입력해주세요")
        String key
) {
}