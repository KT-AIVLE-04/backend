package kt.aivle.content.dto;

import kt.aivle.content.entity.Image;

import java.time.LocalDateTime;

/**
 * 이미지 응답 DTO
 *
 * 이미지 특화 정보를 포함한 응답 클래스
 */
public class ImageResponse extends ContentResponse {

    private Integer width;
    private Integer height;
    private String resolution;
    private Double aspectRatio;
    private String aspectRatioType; // landscape, portrait, square

    // 기본 생성자
    public ImageResponse() {
        super();
    }

    // 생성자
    public ImageResponse(Long id, String title, String originalFilename, String url,
                         String thumbnailUrl, Long fileSize, String contentType,
                         LocalDateTime createdAt, LocalDateTime updatedAt,
                         Integer width, Integer height) {
        super(id, title, originalFilename, url, thumbnailUrl, fileSize, contentType,
                kt.aivle.content.entity.ContentType.IMAGE, createdAt, updatedAt);
        this.width = width;
        this.height = height;
        this.resolution = formatResolution(width, height);
        this.aspectRatio = calculateAspectRatio(width, height);
        this.aspectRatioType = determineAspectRatioType(width, height);
    }

    // Entity에서 DTO로 변환하는 정적 팩토리 메소드
    public static ImageResponse from(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getTitle(),
                image.getOriginalFilename(),
                image.getS3Url(),
                image.getThumbnailUrl(),
                image.getFileSize(),
                image.getContentType(),
                image.getCreatedAt(),
                image.getUpdatedAt(),
                image.getWidth(),
                image.getHeight()
        );
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

    // 가로세로 비율 타입 결정
    private String determineAspectRatioType(Integer width, Integer height) {
        if (width == null || height == null) {
            return "unknown";
        }

        if (width.equals(height)) {
            return "square";
        } else if (width > height) {
            return "landscape";
        } else {
            return "portrait";
        }
    }

    // 해상도 품질 등급 반환
    public String getQualityGrade() {
        if (width == null || height == null) {
            return "unknown";
        }

        int pixels = width * height;

        if (pixels >= 3840 * 2160) return "4K+";
        if (pixels >= 1920 * 1080) return "FHD";
        if (pixels >= 1280 * 720) return "HD";
        if (pixels >= 854 * 480) return "SD";
        return "Low";
    }

    // Getters and Setters
    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
        this.resolution = formatResolution(width, this.height);
        this.aspectRatio = calculateAspectRatio(width, this.height);
        this.aspectRatioType = determineAspectRatioType(width, this.height);
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
        this.resolution = formatResolution(this.width, height);
        this.aspectRatio = calculateAspectRatio(this.width, height);
        this.aspectRatioType = determineAspectRatioType(this.width, height);
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

    public String getAspectRatioType() {
        return aspectRatioType;
    }

    public void setAspectRatioType(String aspectRatioType) {
        this.aspectRatioType = aspectRatioType;
    }
}