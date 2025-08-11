package kt.aivle.content.dto;

import kt.aivle.content.entity.Content;
import kt.aivle.content.entity.ContentType;

import java.time.LocalDateTime;

/**
 * 콘텐츠 응답 DTO
 *
 * 클라이언트에게 전달할 콘텐츠 정보를 담는 클래스
 * Entity의 민감한 정보(S3 키 등)를 숨기고 필요한 정보만 노출
 */
public class ContentResponse {

    private Long id;
    private String title;
    private String originalFilename;
    private String url;
    private String thumbnailUrl;
    private Long fileSize;
    private String formattedFileSize;
    private String contentType;
    private ContentType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public ContentResponse() {}

    // 생성자
    public ContentResponse(Long id, String title, String originalFilename, String url,
                           String thumbnailUrl, Long fileSize, String contentType,
                           ContentType type, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.originalFilename = originalFilename;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.fileSize = fileSize;
        this.formattedFileSize = formatFileSize(fileSize);
        this.contentType = contentType;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Entity에서 DTO로 변환하는 정적 팩토리 메소드
    public static ContentResponse from(Content content) {
        String thumbnailUrl = null;

        // 타입별로 썸네일 URL 설정
        if (content instanceof kt.aivle.content.entity.Image) {
            kt.aivle.content.entity.Image image = (kt.aivle.content.entity.Image) content;
            thumbnailUrl = image.getThumbnailUrl();
        } else if (content instanceof kt.aivle.content.entity.Video) {
            kt.aivle.content.entity.Video video = (kt.aivle.content.entity.Video) content;
            thumbnailUrl = video.getThumbnailUrl();
        }

        return new ContentResponse(
                content.getId(),
                content.getTitle(),
                content.getOriginalFilename(),
                content.getS3Url(),
                thumbnailUrl,
                content.getFileSize(),
                content.getContentType(),
                content.getType(),
                content.getCreatedAt(),
                content.getUpdatedAt()
        );
    }

    // 파일 크기를 사람이 읽기 쉬운 형태로 변환
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        return String.format("%.1f %s",
                bytes / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
        this.formattedFileSize = formatFileSize(fileSize);
    }

    public String getFormattedFileSize() {
        return formattedFileSize;
    }

    public void setFormattedFileSize(String formattedFileSize) {
        this.formattedFileSize = formattedFileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public ContentType getType() {
        return type;
    }

    public void setType(ContentType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}