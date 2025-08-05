// ContentFilterDto.java
package kt.aivle.content.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentFilterDto {
    private String sortBy = "createdDate"; // createdDate, title, fileSize
    private String sortDirection = "desc"; // asc, desc
    private Boolean includeDeleted = false;
    private String fileFormat;
    private Boolean isShorts; // 영상용
    private Boolean isCompressed; // 이미지용
}