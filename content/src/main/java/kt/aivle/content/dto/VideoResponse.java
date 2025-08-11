package kt.aivle.content.dto;

import kt.aivle.content.entity.Video;

import java.time.LocalDateTime;

/**
 * 영상 응답 DTO
 *
 * 영상 특화 정보를 포함한 응답 클래스
 */
public class VideoResponse extends ContentResponse {

    private Integer durationSeconds;
    private String formattedDuration;
    private Integer width;
    private Integer height;
    private String resolution;
    private Double aspectRatio;
    private Integer bitrate;
    private Double frameRate;
    private String codec;
    private Boolean isShort;
    private String durationCategory; // Shorts, Short, Medium, Long
    private String qualityGrade; // 4K, 1080p, 720p, 480p, 360p

    // 기본 생성자
    public VideoResponse() {
        super();
    }

    // 생성자
    public VideoResponse(Long id, String title, String originalFilename, String url,
                         String thumbnailUrl, Long fileSize, String contentType,
                         LocalDateTime createdAt, LocalDateTime updatedAt,
                         Integer durationSeconds, Integer width, Integer height,
                         Integer bitrate, Double frameRate, String codec, Boolean isShort) {
        super(id, title, originalFilename, url, thumbnailUrl, fileSize, contentType,
                kt.aivle.content.entity.ContentType.VIDEO, createdAt, updatedAt);
        this.durationSeconds = durationSeconds;
        this.formattedDuration = formatDuration(durationSeconds);
        this.width = width;
        this.height = height;
        this.resolution = formatResolution(width, height);
        this.aspectRatio = calculateAspectRatio(width, height);
        this.bitrate = bitrate;
        this.frameRate = frameRate;
        this.codec = codec;
        this.isShort = isShort != null ? isShort : false;
        this.durationCategory = determineDurationCategory(durationSeconds);
        this.qualityGrade = determineQualityGrade(width);
    }

    // Entity에서 DTO로 변환하는 정적 팩토리 메소드
    public static VideoResponse from(Video video) {
        return new VideoResponse(
                video.getId(),
                video.getTitle(),
                video.getOriginalFilename(),
                video.getS3Url(),
                video.getThumbnailUrl(),
                video.getFileSize(),
                video.getContentType(),
                video.getCreatedAt(),
                video.getUpdatedAt(),
                video.getDurationSeconds(),
                video.getWidth(),
                video.getHeight(),
                video.getBitrate(),
                video.getFrameRate(),
                video.getCodec(),
                video.getIsShort()
        );
    }

    // 영상 길이 포맷팅
    private String formatDuration(Integer durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return "00:00";
        }

        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    // 해상도 문자열 포맷
    private String formatResolution(Integer width, Integer height) {
        if (width != null && height != null) {
            return width + "x" + height;
        }
        return null;
    }

    // 가로세로 비율 계산
    private Double calculateAspectRatio(Integer width, Integer height) {
        if (width != null && height != null && height > 0) {
            return Math.round((double) width / height * 100.0) / 100.0;
        }
        return null;
    }

    // 영상 길이 카테고리 결정
    private String determineDurationCategory(Integer durationSeconds) {
        if (durationSeconds == null) return "unknown";

        if (durationSeconds <= 60) return "Shorts";
        if (durationSeconds <= 300) return "Short";    // 5분 이하
        if (durationSeconds <= 1800) return "Medium";  // 30분 이하
        return "Long";
    }

    // 화질 등급 결정
    private String determineQualityGrade(Integer width) {
        if (width == null) return "unknown";

        if (width >= 3840) return "4K";
        if (width >= 1920) return "1080p";
        if (width >= 1280) return "720p";
        if (width >= 854) return "480p";
        return "360p";
    }

    // 영상 길이를 사람이 읽기 쉬운 형태로 반환
    public String getHumanReadableDuration() {
        if (durationSeconds == null || durationSeconds <= 0) {
            return "0초";
        }

        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;

        StringBuilder duration = new StringBuilder();

        if (hours > 0) {
            duration.append(hours).append("시간 ");
        }
        if (minutes > 0) {
            duration.append(minutes).append("분 ");
        }
        if (seconds > 0 || duration.length() == 0) {
            duration.append(seconds).append("초");
        }

        return duration.toString().trim();
    }

    // 비트레이트를 사람이 읽기 쉬운 형태로 반환
    public String getFormattedBitrate() {
        if (bitrate == null) return null;

        if (bitrate >= 1000) {
            return String.format("%.1f Mbps", bitrate / 1000.0);
        }
        return bitrate + " kbps";
    }

    // Getters and Setters
    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
        this.formattedDuration = formatDuration(durationSeconds);
        this.durationCategory = determineDurationCategory(durationSeconds);
        this.isShort = durationSeconds != null && durationSeconds <= 60;
    }

    public String getFormattedDuration() {
        return formattedDuration;
    }

    public void setFormattedDuration(String formattedDuration) {
        this.formattedDuration = formattedDuration;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
        this.resolution = formatResolution(width, this.height);
        this.aspectRatio = calculateAspectRatio(width, this.height);
        this.qualityGrade = determineQualityGrade(width);
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
        this.resolution = formatResolution(this.width, height);
        this.aspectRatio = calculateAspectRatio(this.width, height);
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Double getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(Double aspectRatio) {
        this.aspectRatio = aspectRatio;
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

    public String getDurationCategory() {
        return durationCategory;
    }

    public void setDurationCategory(String durationCategory) {
        this.durationCategory = durationCategory;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }
}