package kt.aivle.content.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class FileValidationService {

    private static final Logger logger = LoggerFactory.getLogger(FileValidationService.class);

    @Value("${media.processing.video.supported-formats}")
    private String videoFormats;

    @Value("${media.processing.video.max-size}")
    private long videoMaxSize;

    @Value("${media.processing.image.supported-formats}")
    private String imageFormats;

    @Value("${media.processing.image.max-size}")
    private long imageMaxSize;

    // 비디오 MIME 타입
    private static final List<String> VIDEO_MIME_TYPES = Arrays.asList(
            "video/mp4", "video/quicktime", "video/avi", "video/x-msvideo", "video/x-ms-wmv"
    );

    // 이미지 MIME 타입
    private static final List<String> IMAGE_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    // 악성 파일 확장자 (보안)
    private static final List<String> BLOCKED_EXTENSIONS = Arrays.asList(
            "exe", "bat", "cmd", "scr", "pif", "com", "jar", "js", "vbs", "ps1"
    );

    /**
     * 비디오 파일 검증
     */
    public FileValidationResult validateVideoFile(MultipartFile file) {
        // 기본 검증
        FileValidationResult basicResult = validateBasicFile(file);
        if (!basicResult.isValid()) {
            return basicResult;
        }

        // 확장자 검증
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        List<String> allowedVideoExtensions = Arrays.asList(videoFormats.toLowerCase().split(","));

        if (!allowedVideoExtensions.contains(extension)) {
            return FileValidationResult.invalid(
                    String.format("지원하지 않는 비디오 포맷입니다. 지원 포맷: %s", videoFormats)
            );
        }

        // MIME 타입 검증
        String mimeType = file.getContentType();
        if (mimeType == null || !VIDEO_MIME_TYPES.contains(mimeType.toLowerCase())) {
            logger.warn("의심스러운 비디오 MIME 타입: {} (파일명: {})", mimeType, file.getOriginalFilename());
            // MIME 타입이 정확하지 않아도 확장자가 맞으면 경고만 로그
        }

        // 파일 크기 검증
        if (file.getSize() > videoMaxSize) {
            return FileValidationResult.invalid(
                    String.format("파일 크기가 너무 큽니다. 최대 크기: %dMB", videoMaxSize / (1024 * 1024))
            );
        }

        // 최소 크기 검증 (너무 작은 파일은 의심)
        if (file.getSize() < 1024) { // 1KB 미만
            return FileValidationResult.invalid("파일이 너무 작습니다.");
        }

        logger.info("비디오 파일 검증 성공: {} (크기: {}MB)",
                file.getOriginalFilename(), file.getSize() / (1024.0 * 1024.0));

        return FileValidationResult.valid();
    }

    /**
     * 이미지 파일 검증
     */
    public FileValidationResult validateImageFile(MultipartFile file) {
        // 기본 검증
        FileValidationResult basicResult = validateBasicFile(file);
        if (!basicResult.isValid()) {
            return basicResult;
        }

        // 확장자 검증
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        List<String> allowedImageExtensions = Arrays.asList(imageFormats.toLowerCase().split(","));

        if (!allowedImageExtensions.contains(extension)) {
            return FileValidationResult.invalid(
                    String.format("지원하지 않는 이미지 포맷입니다. 지원 포맷: %s", imageFormats)
            );
        }

        // MIME 타입 검증
        String mimeType = file.getContentType();
        if (mimeType == null || !IMAGE_MIME_TYPES.contains(mimeType.toLowerCase())) {
            return FileValidationResult.invalid(
                    String.format("올바르지 않은 이미지 파일입니다. MIME 타입: %s", mimeType)
            );
        }

        // 파일 크기 검증
        if (file.getSize() > imageMaxSize) {
            return FileValidationResult.invalid(
                    String.format("파일 크기가 너무 큽니다. 최대 크기: %dMB", imageMaxSize / (1024 * 1024))
            );
        }

        // 최소 크기 검증
        if (file.getSize() < 100) { // 100bytes 미만
            return FileValidationResult.invalid("파일이 너무 작습니다.");
        }

        // 이미지 파일 시그니처 검증 (추가 보안)
        FileValidationResult signatureResult = validateImageSignature(file);
        if (!signatureResult.isValid()) {
            return signatureResult;
        }

        logger.info("이미지 파일 검증 성공: {} (크기: {}KB)",
                file.getOriginalFilename(), file.getSize() / 1024.0);

        return FileValidationResult.valid();
    }

    /**
     * 기본 파일 검증
     */
    private FileValidationResult validateBasicFile(MultipartFile file) {
        // 파일 존재 여부
        if (file == null || file.isEmpty()) {
            return FileValidationResult.invalid("파일이 없습니다.");
        }

        // 파일명 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return FileValidationResult.invalid("파일명이 없습니다.");
        }

        // 파일명 길이 검증
        if (originalFilename.length() > 255) {
            return FileValidationResult.invalid("파일명이 너무 깁니다. (최대 255자)");
        }

        // 악성 확장자 검증
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (BLOCKED_EXTENSIONS.contains(extension)) {
            return FileValidationResult.invalid("업로드할 수 없는 파일 형식입니다.");
        }

        // 파일명에 특수문자 검증
        if (originalFilename.matches(".*[<>:\"|?*].*")) {
            return FileValidationResult.invalid("파일명에 사용할 수 없는 문자가 포함되어 있습니다.");
        }

        return FileValidationResult.valid();
    }

    /**
     * 이미지 파일 시그니처 검증 (매직 넘버 체크)
     */
    private FileValidationResult validateImageSignature(MultipartFile file) {
        try {
            byte[] fileBytes = file.getInputStream().readNBytes(10); // 처음 10바이트만 읽기

            // JPEG 시그니처: FF D8 FF
            if (fileBytes.length >= 3 &&
                    (fileBytes[0] & 0xFF) == 0xFF &&
                    (fileBytes[1] & 0xFF) == 0xD8 &&
                    (fileBytes[2] & 0xFF) == 0xFF) {
                return FileValidationResult.valid();
            }

            // PNG 시그니처: 89 50 4E 47 0D 0A 1A 0A
            if (fileBytes.length >= 8 &&
                    (fileBytes[0] & 0xFF) == 0x89 &&
                    (fileBytes[1] & 0xFF) == 0x50 &&
                    (fileBytes[2] & 0xFF) == 0x4E &&
                    (fileBytes[3] & 0xFF) == 0x47) {
                return FileValidationResult.valid();
            }

            // WebP 시그니처: 52 49 46 46 ... 57 45 42 50
            if (fileBytes.length >= 8 &&
                    (fileBytes[0] & 0xFF) == 0x52 &&
                    (fileBytes[1] & 0xFF) == 0x49 &&
                    (fileBytes[2] & 0xFF) == 0x46 &&
                    (fileBytes[3] & 0xFF) == 0x46) {
                // 추가로 WebP 시그니처 확인 필요시
                return FileValidationResult.valid();
            }

            logger.warn("이미지 파일 시그니처 불일치: {}", file.getOriginalFilename());
            return FileValidationResult.invalid("올바른 이미지 파일이 아닙니다.");

        } catch (Exception e) {
            logger.error("이미지 시그니처 검증 실패: {}", file.getOriginalFilename(), e);
            return FileValidationResult.invalid("파일 검증 중 오류가 발생했습니다.");
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 안전한 파일명 생성
     */
    public String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unknown";
        }

        // 특수문자 제거 및 공백을 언더스코어로 변경
        return filename.replaceAll("[<>:\"|?*]", "")
                .replaceAll("\\s+", "_")
                .trim();
    }

    // === 결과 클래스 ===

    public static class FileValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private FileValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static FileValidationResult valid() {
            return new FileValidationResult(true, null);
        }

        public static FileValidationResult invalid(String errorMessage) {
            return new FileValidationResult(false, errorMessage);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }
}