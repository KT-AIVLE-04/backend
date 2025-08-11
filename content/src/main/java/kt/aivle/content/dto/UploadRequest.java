package kt.aivle.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 요청 DTO
 *
 * 파일 업로드 시 클라이언트에서 전달하는 정보를 담는 클래스
 */
public class UploadRequest {

    @NotNull(message = "파일은 필수입니다.")
    private MultipartFile file;

    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;

    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다.")
    private String title;

    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다.")
    private String description;

    private String tags; // 쉼표로 구분된 태그들

    private Boolean isPublic = false; // 공개 여부

    // 기본 생성자
    public UploadRequest() {}

    // 생성자
    public UploadRequest(MultipartFile file, String userId, String title, String description, String tags, Boolean isPublic) {
        this.file = file;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.isPublic = isPublic != null ? isPublic : false;
    }

    // 파일 크기 검증
    public boolean isFileSizeValid(long maxSize) {
        return file != null && file.getSize() <= maxSize;
    }

    // 파일 형식 검증
    public boolean isFileTypeValid(String[] allowedTypes) {
        if (file == null || file.getContentType() == null) {
            return false;
        }

        String contentType = file.getContentType().toLowerCase();
        for (String allowedType : allowedTypes) {
            if (contentType.contains(allowedType.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // 제목이 없으면 파일명에서 생성
    public String getEffectiveTitle() {
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        }

        if (file != null && file.getOriginalFilename() != null) {
            String filename = file.getOriginalFilename();
            int lastDotIndex = filename.lastIndexOf(".");
            if (lastDotIndex > 0) {
                return filename.substring(0, lastDotIndex);
            }
            return filename;
        }

        return "제목 없음";
    }

    // 태그 리스트로 변환
    public String[] getTagsArray() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }

        return tags.split(",");
    }

    // Getters and Setters
    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}