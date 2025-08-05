// UploadResultDto.java
package kt.aivle.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultDto {
    private String filePath;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String message;
    private Boolean success = true;
}