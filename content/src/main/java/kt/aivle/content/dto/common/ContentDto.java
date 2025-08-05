// ContentDto.java - 통합 콘텐츠 DTO
package kt.aivle.content.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDto {
    private Long id;
    private String title;
    private Boolean isAiGenerated;
    private LocalDateTime createdDate;
    private Long fileSize;
    private String previewPath; // 썸네일 또는 이미지 경로
    private String contentType; // "VIDEO" 또는 "IMAGE"
    private String format; // 파일 포맷
    private String resolution;
    private String formattedFileSize;

    // 영상 전용 필드
    private Integer duration;
    private Boolean isShorts;

    // 이미지 전용 필드
    private String keywords;
    private Boolean isCompressed;
}