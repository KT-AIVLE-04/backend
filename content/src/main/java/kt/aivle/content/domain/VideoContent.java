package kt.aivle.content.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "video_contents")
public class VideoContent extends BaseContent {

    @Column(columnDefinition = "TEXT")
    private String scenario; // AI 생성 시나리오

    @Column
    private Integer duration; // 영상 길이(초)

    @Column(length = 500)
    private String thumbnailPath; // 썸네일 경로 (0:01초 캡처)

    @Column(length = 50)
    private String videoFormat; // MP4, MOV, AVI, WMV

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column(nullable = false)
    private Boolean isShorts = false; // 숏츠 여부 (60초 이하)

    @Column
    private String resolution; // 해상도 (예: 1920x1080)

    @Column
    private Integer bitrate; // 비트레이트

    @Column
    private Double frameRate; // 프레임레이트

    // Constructors
    public VideoContent() {
        super();
    }

    public VideoContent(String title, Boolean isAiGenerated, String filePath,
                        String fileName, Long fileSize, String contentType) {
        super(title, isAiGenerated, filePath, fileName, fileSize, contentType);
    }

    // Builder pattern을 위한 정적 메서드
    public static VideoContentBuilder builder() {
        return new VideoContentBuilder();
    }

    // Getters and Setters
    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
        // 60초 이하면 자동으로 숏츠로 설정
        if (duration != null && duration <= 60) {
            this.isShorts = true;
        }
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getVideoFormat() {
        return videoFormat;
    }

    public void setVideoFormat(String videoFormat) {
        this.videoFormat = videoFormat;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
        updateResolution();
    }

    public Boolean getIsShorts() {
        return isShorts;
    }

    public void setIsShorts(Boolean isShorts) {
        this.isShorts = isShorts;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
    }

    public Double getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(Double frameRate) {
        this.frameRate = frameRate;
    }

    // 해상도 업데이트 메서드
    private void updateResolution() {
        if (width != null && height != null) {
            this.resolution = width + "x" + height;
        }
    }

    // Builder 클래스
    public static class VideoContentBuilder {
        private final VideoContent videoContent;

        public VideoContentBuilder() {
            this.videoContent = new VideoContent();
        }

        public VideoContentBuilder title(String title) {
            videoContent.setTitle(title);
            return this;
        }

        public VideoContentBuilder isAiGenerated(Boolean isAiGenerated) {
            videoContent.setIsAiGenerated(isAiGenerated);
            return this;
        }

        public VideoContentBuilder filePath(String filePath) {
            videoContent.setFilePath(filePath);
            return this;
        }

        public VideoContentBuilder fileName(String fileName) {
            videoContent.setFileName(fileName);
            return this;
        }

        public VideoContentBuilder fileSize(Long fileSize) {
            videoContent.setFileSize(fileSize);
            return this;
        }

        public VideoContentBuilder contentType(String contentType) {
            videoContent.setContentType(contentType);
            return this;
        }

        public VideoContentBuilder scenario(String scenario) {
            videoContent.setScenario(scenario);
            return this;
        }

        public VideoContentBuilder duration(Integer duration) {
            videoContent.setDuration(duration);
            return this;
        }

        public VideoContentBuilder thumbnailPath(String thumbnailPath) {
            videoContent.setThumbnailPath(thumbnailPath);
            return this;
        }

        public VideoContentBuilder videoFormat(String videoFormat) {
            videoContent.setVideoFormat(videoFormat);
            return this;
        }

        public VideoContentBuilder width(Integer width) {
            videoContent.setWidth(width);
            return this;
        }

        public VideoContentBuilder height(Integer height) {
            videoContent.setHeight(height);
            return this;
        }

        public VideoContentBuilder bitrate(Integer bitrate) {
            videoContent.setBitrate(bitrate);
            return this;
        }

        public VideoContentBuilder frameRate(Double frameRate) {
            videoContent.setFrameRate(frameRate);
            return this;
        }

        public VideoContent build() {
            return videoContent;
        }
    }
}