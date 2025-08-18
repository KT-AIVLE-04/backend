package kt.aivle.content.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateContentRequest(
        Long userId,
        Long storeId,
        Long id,
        @NotBlank(message = "제목을 입력해주세요.")
        String title
) {
}