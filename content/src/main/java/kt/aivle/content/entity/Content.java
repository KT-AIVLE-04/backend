package kt.aivle.content.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "contents")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "content_type")
public abstract class Content extends BaseEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "s3_url", nullable = false, length = 500)
    private String s3Url;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ContentType type;

    @Column(name = "user_id")
    private String userId; // 실제 프로젝트에서는 User 엔티티와 연관관계 설정

    // 기본 생성자
    protected Content() {}

    // 생성자
    public Content(String title, String originalFilename, String s3Url, String s3Key,
                   Long fileSize, String contentType, ContentType type, String userId) {
        this.title = title;
        this.originalFilename = originalFilename;
        this.s3Url = s3Url;
        this.s3Key = s3Key;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.type = type;
        this.userId = userId;
    }

    // Getters and Setters
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

    public String getS3Url() {
        return s3Url;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}