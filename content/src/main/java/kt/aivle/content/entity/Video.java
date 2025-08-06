package kt.aivle.content.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video extends MediaFile {

    @Column(name = "scenario", columnDefinition = "TEXT")
    private String scenario;

    @Column(name = "duration")
    private Integer duration; // 영상 길이 (초 단위)

    @Column(name = "resolution_width")
    private Integer resolutionWidth;

    @Column(name = "resolution_height")
    private Integer resolutionHeight;

    @Column(name = "frame_rate")
    private Double frameRate;

    @Column(name = "bitrate")
    private Long bitrate;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_type", nullable = false)
    private VideoType videoType;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    private ProcessingStatus processingStatus;

    @Column(name = "preview_start_time")
    private Integer previewStartTime; // 미리보기 시작 시간 (초)

    @Column(name = "is_shorts")
    private Boolean isShorts = false; // 숏츠 여부

    public enum VideoType {
        NORMAL("일반 영상"),
        SHORTS("숏츠");

        private final String description;

        VideoType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
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
}