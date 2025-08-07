package kt.aivle.store.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import kt.aivle.store.domain.model.Industry;

public record CreateStoreRequest(
        @NotBlank(message = "가게 이름을 입력해주세요.")
        String name,

        @NotBlank(message = "주소를 입력해주세요.")
        String address,

        @NotBlank(message = "연락처를 입력해주세요.")
        @Pattern(regexp = "^(0\\d{1,2})-\\d{3,4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다.")
        String phoneNumber,

        String businessNumber,

        Double latitude,
        Double longitude,

        @NotNull(message = "업종을 선택해주세요.")
        Industry industry
) {
}
