// ImageContentDto.java
package kt.aivle.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageContentDto {
    private Long id;
    private String title;
    private String keywords;
    private Boolean isAiGenerated;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String fileName;
    private Long fileSize;
    private String filePath; // 미리보기용
    private String imageFormat;
    private String resolution;
    private Boolean isCompressed;
}