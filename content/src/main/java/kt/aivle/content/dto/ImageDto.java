// ===== ImageDto.java =====
package kt.aivle.content.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageDto {
    private Long id;

    @NotBlank(message = "이미지 제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다.")
    private String title;

    @Size(max = 1000, message = "키워드는 1000자를 초과할 수 없습니다.")
    private String keywords;

    private String originalFilename;
    private Long fileSize;
    private String imageUrl;
    private String thumbnailUrl;
    private String contentType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 계산된 필드들
    private String formattedFileSize;
    private String fileExtension;
    private ImageDimensions dimensions;

    // 파일 크기를 사람이 읽기 쉬운 형태로 변환
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";

        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // 파일 확장자 추출
    public String getFileExtension() {
        if (originalFilename == null) return "";
        int lastIndexOf = originalFilename.lastIndexOf(".");
        if (lastIndexOf == -1) return "";
        return originalFilename.substring(lastIndexOf + 1).toLowerCase();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDimensions {
        private Integer width;
        private Integer height;

        @Override
        public String toString() {
            return width + "x" + height;
        }
    }
}