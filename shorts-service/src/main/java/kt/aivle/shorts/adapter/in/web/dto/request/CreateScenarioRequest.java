package kt.aivle.shorts.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateScenarioRequest(
        @NotBlank(message = "프롬프트를 입력해주세요.")
        String prompt,

        @NotBlank(message = "플랫폼을 입력해주세요.")
        String platform,

        @NotBlank(message = "홍보하고싶은 대상을 입력해주세요.")
        String target,

        @NotBlank(message = "홍보 유형을 입력해주세요.")
        String adType,

        @NotEmpty(message = "브랜드 컨셉을 입력해주세요.")
        List<String> brandConcepts
) {
}