package kt.aivle.content.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageConfig {

    private String uploadDir = "uploads";
    private String videoDir = "videos";
    private String imageDir = "images";
    private String thumbnailDir = "thumbnails";

    // 파일 크기 제한 (bytes)
    private long maxVideoSize = 500 * 1024 * 1024; // 500MB
    private long maxImageSize = 10 * 1024 * 1024;  // 10MB

    // 허용된 파일 확장자
    private String[] allowedVideoExtensions = {"mp4", "mov", "avi", "wmv"};
    private String[] allowedImageExtensions = {"jpg", "jpeg", "png", "webp"};

    // 이미지 압축 설정
    private float imageQuality = 0.8f;
    private int maxImageWidth = 1920;
    private int maxImageHeight = 1080;

    // Getters and Setters
    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getVideoDir() {
        return videoDir;
    }

    public void setVideoDir(String videoDir) {
        this.videoDir = videoDir;
    }

    public String getImageDir() {
        return imageDir;
    }

    public void setImageDir(String imageDir) {
        this.imageDir = imageDir;
    }

    public String getThumbnailDir() {
        return thumbnailDir;
    }

    public void setThumbnailDir(String thumbnailDir) {
        this.thumbnailDir = thumbnailDir;
    }

    public long getMaxVideoSize() {
        return maxVideoSize;
    }

    public void setMaxVideoSize(long maxVideoSize) {
        this.maxVideoSize = maxVideoSize;
    }

    public long getMaxImageSize() {
        return maxImageSize;
    }

    public void setMaxImageSize(long maxImageSize) {
        this.maxImageSize = maxImageSize;
    }

    public String[] getAllowedVideoExtensions() {
        return allowedVideoExtensions;
    }

    public void setAllowedVideoExtensions(String[] allowedVideoExtensions) {
        this.allowedVideoExtensions = allowedVideoExtensions;
    }

    public String[] getAllowedImageExtensions() {
        return allowedImageExtensions;
    }

    public void setAllowedImageExtensions(String[] allowedImageExtensions) {
        this.allowedImageExtensions = allowedImageExtensions;
    }

    public float getImageQuality() {
        return imageQuality;
    }

    public void setImageQuality(float imageQuality) {
        this.imageQuality = imageQuality;
    }

    public int getMaxImageWidth() {
        return maxImageWidth;
    }

    public void setMaxImageWidth(int maxImageWidth) {
        this.maxImageWidth = maxImageWidth;
    }

    public int getMaxImageHeight() {
        return maxImageHeight;
    }

    public void setMaxImageHeight(int maxImageHeight) {
        this.maxImageHeight = maxImageHeight;
    }
}