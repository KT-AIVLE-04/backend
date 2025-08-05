// FileMetadata.java - 파일 메타데이터
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
public class FileMetadata {
    private String fileName;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String checksum; // 파일 무결성 체크용
    private LocalDateTime uploadDate;

    // 이미지 메타데이터
    private Integer width;
    private Integer height;
    private String colorSpace;
    private Integer dpi;

    // 영상 메타데이터
    private Integer duration;
    private Integer bitrate;
    private Double frameRate;
}