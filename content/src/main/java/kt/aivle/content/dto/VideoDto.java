// ===== VideoDto.java =====
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
public class VideoDto {
    private Long id;

    @NotBlank(message = "영상 제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다.")
    private String title;

    @Size(max = 2000, message = "시나리오는 2000자를 초과할 수 없습니다.")
    private String scenario;      // 목록용 (제한된 길이)

    private String fullScenario;  // 상세용 (전체 길이)
    private String originalFilename;
    private Long fileSize;
    private String videoUrl;
    private String thumbnailUrl;
    private String contentType;
    private Long duration;        // 초 단위
    private String resolution;    // 예: "1920x1080"
    private Double frameRate;     // fps
    private Long bitrate;         // kbps
    private String codec;         // 비디오 코덱 (H.264, H.265 등)
    private String audioCodec;    // 오디오 코덱

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 계산된 필드들
    private String formattedFileSize;
    private String formattedDuration;
    private String fileExtension;
    private VideoQuality quality;

    // 파일 크기를 사람이 읽기 쉬운 형태로 변환
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";

        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // 영상 길이를 사람이 읽기 쉬한 형태로 변환
    public String getFormattedDuration() {
        if (duration == null) return "0:00";

        long totalSeconds = duration;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    // 파일 확장자 추출
    public String getFileExtension() {
        if (originalFilename == null) return "";
        int lastIndexOf = originalFilename.lastIndexOf(".");
        if (lastIndexOf == -1) return "";
        return originalFilename.substring(lastIndexOf + 1).toLowerCase();
    }

    // 영상 품질 판단
    public VideoQuality getQuality() {
        if (resolution == null) return VideoQuality.UNKNOWN;

        String[] parts = resolution.split("x");
        if (parts.length != 2) return VideoQuality.UNKNOWN;

        try {
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);

            if (width >= 3840 && height >= 2160) return VideoQuality.UHD_4K;
            if (width >= 2560 && height >= 1440) return VideoQuality.QHD;
            if (width >= 1920 && height >= 1080) return VideoQuality.FULL_HD;
            if (width >= 1280 && height >= 720) return VideoQuality.HD;
            return VideoQuality.SD;

        } catch (NumberFormatException e) {
            return VideoQuality.UNKNOWN;
        }
    }

    public enum VideoQuality {
        UHD_4K("4K UHD", "3840x2160+"),
        QHD("QHD", "2560x1440+"),
        FULL_HD("Full HD", "1920x1080"),
        HD("HD", "1280x720"),
        SD("SD", "720p 미만"),
        UNKNOWN("알 수 없음", "");

        private final String displayName;
        private final String description;

        VideoQuality(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}
