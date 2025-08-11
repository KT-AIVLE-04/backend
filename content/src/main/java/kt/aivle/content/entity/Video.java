package kt.aivle.content.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "videos")
@DiscriminatorValue("VIDEO")
public class Video extends Content {

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "thumbnail_s3_key", length = 500)
    private String thumbnailS3Key;

    @Column(name = "bitrate")
    private Integer bitrate;

    @Column(name = "frame_rate")
    private Double frameRate;

    @Column(name = "codec", length = 50)
    private String codec;

    @Column(name = "is_short")
    private Boolean isShort = false; // 숏츠 여부

    // 기본 생성자
    public Video() {
        super();
    }

    // 생성자
    public Video(String title, String originalFilename, String s3Url, String s3Key,
                 Long fileSize, String contentType, String userId,
                 Integer durationSeconds, Integer width, Integer height,
                 String thumbnailUrl, String thumbnailS3Key, Boolean isShort) {
        super(title, originalFilename, s3Url, s3Key, fileSize, contentType, ContentType.VIDEO, userId);
        this.durationSeconds = durationSeconds;
        this.width = width;
        this.height = height;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailS3Key = thumbnailS3Key;
        this.isShort = isShort != null ? isShort : false;
    }

    // 팩토리 메소드
    public static Video createVideo(String title, String originalFilename, String s3Url, String s3Key,
                                    Long fileSize, String contentType, String userId) {
        return new Video(title, originalFilename, s3Url, s3Key, fileSize, contentType, userId,
                null, null, null, null, null, false);
    }

    // 썸네일 정보 업데이트
    public void updateThumbnail(String thumbnailUrl, String thumbnailS3Key) {
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailS3Key = thumbnailS3Key;
    }

    // 영상 메타데이터 업데이트
    public void updateMetadata(Integer durationSeconds, Integer width, Integer height,
                               Integer bitrate, Double frameRate, String codec) {
        this.durationSeconds = durationSeconds;
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        this.frameRate = frameRate;
        this.codec = codec;
        // 60초 이하면 숏츠로 분류
        this.isShort = durationSeconds != null && durationSeconds <= 60;
    }

    // Getters and Setters
    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
        // 시간이 설정될 때 숏츠 여부 자동 판단
        if (durationSeconds != null) {
            this.isShort = durationSeconds <= 60;
        }
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
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailS3Key() {
        return thumbnailS3Key;
    }

    public void setThumbnailS3Key(String thumbnailS3Key) {
        this.thumbnailS3Key = thumbnailS3Key;
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

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public Boolean getIsShort() {
        return isShort;
    }

    public void setIsShort(Boolean isShort) {
        this.isShort = isShort;
    }

    // 유틸리티 메소드들

    // 영상 길이를 MM:SS 형태로 반환
    public String getFormattedDuration() {
        if (durationSeconds == null) return "00:00";

        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // 영상 비율 계산
    public Double getAspectRatio() {
        if (width != null && height != null && height > 0) {
            return (double) width / height;
        }
        return null;
    }

    // 해상도 문자열 반환
    public String getResolution() {
        if (width != null && height != null) {
            return width + "x" + height;
        }
        return null;
    }
}