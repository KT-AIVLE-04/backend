package kt.aivle.content.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image extends MediaFile {

    @Column(name = "keywords", length = 500)
    private String keywords; // 이미지 생성 키워드

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "color_depth")
    private Integer colorDepth; // 색상 깊이 (비트)

    @Enumerated(EnumType.STRING)
    @Column(name = "image_format", nullable = false)
    private ImageFormat imageFormat;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    private ProcessingStatus processingStatus;

    @Column(name = "compressed_url", length = 500)
    private String compressedUrl; // 압축된 이미지 URL

    @Column(name = "alt_text", length = 255)
    private String altText; // 접근성을 위한 대체 텍스트

    public enum ImageFormat {
        JPEG("image/jpeg", "jpg"),
        PNG("image/png", "png"),
        WEBP("image/webp", "webp"),
        GIF("image/gif", "gif");

        private final String mimeType;
        private final String extension;

        ImageFormat(String mimeType, String extension) {
            this.mimeType = mimeType;
            this.extension = extension;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getExtension() {
            return extension;
        }
    }

    public enum ProcessingStatus {
        UPLOADING("업로드 중"),
        PROCESSING("처리 중"),
        COMPLETED("완료"),
        FAILED("실패");

        private final String description;

        ProcessingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 이미지 비율 계산
    public double getAspectRatio() {
        if (height == null || height == 0) {
            return 0.0;
        }
        return (double) width / height;
    }

    // 이미지 크기 문자열 반환
    public String getDimensionString() {
        return width + " × " + height;
    }
}