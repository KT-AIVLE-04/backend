package kt.aivle.content.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "image_contents")
public class ImageContent extends BaseContent {

    @Column(columnDefinition = "TEXT")
    private String scenario; // AI 생성 시나리오

    @Column(length = 500)
    private String keywords; // 이미지 키워드

    @Column(length = 50)
    private String imageFormat; // JPG, PNG, WebP

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column
    private String resolution; // 해상도 (예: 1920x1080)

    @Column
    private String colorSpace; // 색상 공간 (RGB, CMYK 등)

    @Column
    private Integer dpi; // 해상도 (dots per inch)

    @Column
    private Boolean isCompressed = false; // 압축 여부

    @Column
    private String originalFileName; // 원본 파일명

    // Constructors
    public ImageContent() {
        super();
    }

    public ImageContent(String title, Boolean isAiGenerated, String filePath,
                        String fileName, Long fileSize, String contentType) {
        super(title, isAiGenerated, filePath, fileName, fileSize, contentType);
    }

    // Builder pattern을 위한 정적 메서드
    public static ImageContentBuilder builder() {
        return new ImageContentBuilder();
    }

    // Getters and Setters
    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
        updateResolution();
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
        updateResolution();
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(String colorSpace) {
        this.colorSpace = colorSpace;
    }

    public Integer getDpi() {
        return dpi;
    }

    public void setDpi(Integer dpi) {
        this.dpi = dpi;
    }

    public Boolean getIsCompressed() {
        return isCompressed;
    }

    public void setIsCompressed(Boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    // 해상도 업데이트 메서드
    private void updateResolution() {
        if (width != null && height != null) {
            this.resolution = width + "x" + height;
        }
    }

    // Builder 클래스
    public static class ImageContentBuilder {
        private final ImageContent imageContent;

        public ImageContentBuilder() {
            this.imageContent = new ImageContent();
        }

        public ImageContentBuilder title(String title) {
            imageContent.setTitle(title);
            return this;
        }

        public ImageContentBuilder isAiGenerated(Boolean isAiGenerated) {
            imageContent.setIsAiGenerated(isAiGenerated);
            return this;
        }

        public ImageContentBuilder filePath(String filePath) {
            imageContent.setFilePath(filePath);
            return this;
        }

        public ImageContentBuilder fileName(String fileName) {
            imageContent.setFileName(fileName);
            return this;
        }

        public ImageContentBuilder fileSize(Long fileSize) {
            imageContent.setFileSize(fileSize);
            return this;
        }

        public ImageContentBuilder contentType(String contentType) {
            imageContent.setContentType(contentType);
            return this;
        }

        public ImageContentBuilder scenario(String scenario) {
            imageContent.setScenario(scenario);
            return this;
        }

        public ImageContentBuilder keywords(String keywords) {
            imageContent.setKeywords(keywords);
            return this;
        }

        public ImageContentBuilder imageFormat(String imageFormat) {
            imageContent.setImageFormat(imageFormat);
            return this;
        }

        public ImageContentBuilder width(Integer width) {
            imageContent.setWidth(width);
            return this;
        }

        public ImageContentBuilder height(Integer height) {
            imageContent.setHeight(height);
            return this;
        }

        public ImageContentBuilder colorSpace(String colorSpace) {
            imageContent.setColorSpace(colorSpace);
            return this;
        }

        public ImageContentBuilder dpi(Integer dpi) {
            imageContent.setDpi(dpi);
            return this;
        }

        public ImageContentBuilder isCompressed(Boolean isCompressed) {
            imageContent.setIsCompressed(isCompressed);
            return this;
        }

        public ImageContentBuilder originalFileName(String originalFileName) {
            imageContent.setOriginalFileName(originalFileName);
            return this;
        }

        public ImageContent build() {
            return imageContent;
        }
    }
}