// ImageUploadDto.java
package kt.aivle.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadDto {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    private String title;

    @Size(max = 500, message = "키워드는 500자를 초과할 수 없습니다")
    private String keywords;

    @Size(max = 1000, message = "시나리오는 1000자를 초과할 수 없습니다")
    private String scenario;

    private Boolean isAiGenerated = false;

    // 메타데이터
    private String description;
    private String altText; // 접근성을 위한 대체 텍스트

    // 이미지 처리 옵션
    private Boolean enableCompression = true;
    private Integer targetWidth;
    private Integer targetHeight;
    private Float quality; // 0.1 ~ 1.0
}