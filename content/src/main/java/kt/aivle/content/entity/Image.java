package kt.aivle.content.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "images")
@DiscriminatorValue("IMAGE")
public class Image extends Content {

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "thumbnail_s3_key", length = 500)
    private String thumbnailS3Key;

    // 기본 생성자
    public Image() {
        super();
    }

    // 생성자
    public Image(String title, String originalFilename, String s3Url, String s3Key,
                 Long fileSize, String contentType, String userId,
                 Integer width, Integer height, String thumbnailUrl, String thumbnailS3Key) {
        super(title, originalFilename, s3Url, s3Key, fileSize, contentType, ContentType.IMAGE, userId);
        this.width = width;
        this.height = height;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailS3Key = thumbnailS3Key;
    }

    // 팩토리 메소드
    public static Image createImage(String title, String originalFilename, String s3Url, String s3Key,
                                    Long fileSize, String contentType, String userId) {
        return new Image(title, originalFilename, s3Url, s3Key, fileSize, contentType, userId,
                null, null, null, null);
    }

    // 썸네일 정보 업데이트
    public void updateThumbnail(String thumbnailUrl, String thumbnailS3Key) {
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailS3Key = thumbnailS3Key;
    }

    // 이미지 크기 업데이트
    public void updateDimensions(Integer width, Integer height) {
        this.width = width;
        this.height = height;
    }

    // Getters and Setters
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
        return thumbnailUrl != null ? thumbnailUrl : getS3Url(); // 썸네일이 없으면 원본 이미지 반환
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

    // 이미지 비율 계산
    public Double getAspectRatio() {
        if (width != null && height != null && height > 0) {
            return (double) width / height;
        }
        return null;
    }
}