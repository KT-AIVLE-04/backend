// FileUtils.java
package kt.aivle.content.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;

@Component
public class FileUtils {

    /**
     * 파일 확장자 추출
     */
    public static String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 파일명에서 확장자 제거
     */
    public static String removeFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return filename;
        }

        return filename.substring(0, lastDotIndex);
    }

    /**
     * 파일 크기를 읽기 쉬운 형태로 포맷팅
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes <= 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(sizeInBytes) / Math.log10(1024));

        DecimalFormat df = new DecimalFormat("#,##0.#");
        return df.format(sizeInBytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 안전한 파일명 생성 (특수문자 제거)
     */
    public static String sanitizeFileName(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "unnamed";
        }

        // 위험한 문자들을 언더스코어로 대체
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .trim();
    }

    /**
     * MIME 타입에서 파일 확장자 추출
     */
    public static String getExtensionFromMimeType(String mimeType) {
        if (!StringUtils.hasText(mimeType)) {
            return "";
        }

        return switch (mimeType.toLowerCase()) {
            case "video/mp4" -> "mp4";
            case "video/quicktime" -> "mov";
            case "video/x-msvideo" -> "avi";
            case "video/x-ms-wmv" -> "wmv";
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "";
        };
    }

    /**
     * 파일 확장자에서 MIME 타입 추출
     */
    public static String getMimeTypeFromExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            return "application/octet-stream";
        }

        return switch (extension.toLowerCase()) {
            case "mp4" -> "video/mp4";
            case "mov" -> "video/quicktime";
            case "avi" -> "video/x-msvideo";
            case "wmv" -> "video/x-ms-wmv";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    /**
     * 파일 경로에서 파일명만 추출
     */
    public static String getFileNameFromPath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return "";
        }

        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }
}