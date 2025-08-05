// VideoContentDetailDto.java
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
public class VideoContentDetailDto {
    private Long id;
    private String title;
    private String scenario;
    private Boolean isAiGenerated;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    // 파일 정보
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;

    // 영상 정보
    private Integer duration;
    private String videoFormat;
    private Integer width;
    private Integer height;
    private String resolution;
    private Integer bitrate;
    private Double frameRate;
    private Boolean isShorts;
    private String thumbnailPath;

    // YouTube형 정보
    private String formattedDuration; // "01:23" 형식
    private String formattedFileSize; // "15.2 MB" 형식
    private String qualityLabel; // "HD", "FHD" 등
}