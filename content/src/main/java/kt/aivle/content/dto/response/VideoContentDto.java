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
public class VideoContentDto {
    private Long id;
    private String title;
    private Boolean isAiGenerated;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String fileName;
    private Long fileSize;
    private String thumbnailPath;
    private Integer duration;
    private String videoFormat;
    private String resolution;
    private Boolean isShorts;
    private String scenario; // 글자수 제한 표기용
}