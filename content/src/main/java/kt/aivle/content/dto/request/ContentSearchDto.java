// ContentSearchDto.java
package kt.aivle.content.dto.request;

import kt.aivle.content.dto.common.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentSearchDto {
    private ContentType contentType; // VIDEO, IMAGE, ALL
    private Boolean isAiGenerated;
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String keywords;
    private String format; // 파일 포맷
    private Long minFileSize;
    private Long maxFileSize;
    private Integer minWidth;
    private Integer maxWidth;
    private Integer minHeight;
    private Integer maxHeight;
}
