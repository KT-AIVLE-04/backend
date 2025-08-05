// ImageContentDetailDto.java
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
public class ImageContentDetailDto {
    private Long id;
    private String title;
    private String keywords;
    private String scenario;
    private Boolean isAiGenerated;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    // 파일 정보
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String originalFileName;

    // 이미지 정보
    private String imageFormat;
    private Integer width;
    private Integer height;
    private String resolution;
    private String colorSpace;
    private Integer dpi;
    private Boolean isCompressed;

    // 포맷팅된 정보
    private String formattedFileSize; // "2.5 MB" 형식
    private String aspectRatio; // "16:9" 형식
}