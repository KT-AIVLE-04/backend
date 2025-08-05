// FileResourceDto.java
package kt.aivle.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResourceDto {
    private Resource resource;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
}
