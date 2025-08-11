package kt.aivle.shorts.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSceneRequest(
        @NotBlank(message = "세션 ID를 입력해주세요.")
        String sessionId,

        @NotBlank(message = "시나리오 제목을 입력해주세요.")
        String title,

        @NotBlank(message = "시나리오 내용을 입력해주세요.")
        String content,

        @NotNull(message = "시간을 입력해주세요.")
        Integer adDuration
) {
}