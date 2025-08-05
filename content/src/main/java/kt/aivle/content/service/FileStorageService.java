package kt.aivle.content.service;

import kt.aivle.content.config.FileStorageConfig;
import kt.aivle.content.dto.common.FileMetadata;
import kt.aivle.content.dto.response.UploadResultDto;
import kt.aivle.content.exception.FileProcessingException;
import kt.aivle.content.exception.InvalidFileException;
import kt.aivle.content.util.FileUtils;
import kt.aivle.content.util.ImageUtils;
import kt.aivle.content.util.VideoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageConfig fileConfig;
    private final VideoUtils videoUtils;
    private final ImageUtils imageUtils;
    private final FileUtils fileUtils;

    /**
     * 영상 파일 저장
     */
    public UploadResultDto saveVideoFile(MultipartFile file) {
        try {
            validateVideoFile(file);

            // 디렉토리 생성
            Path videoDir = createDirectoryIfNotExists(fileConfig.getUploadDir(), fileConfig.getVideoDir());

            // 고유한 파일명 생성
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            Path targetPath = videoDir.resolve(fileName);

            // 파일 저장
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 체크섬 생성
            String checksum = generateChecksum(targetPath);

            log.info("Video file saved: {}", targetPath);

            return UploadResultDto.builder()
                    .filePath(targetPath.toString())
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .message("영상 파일이 성공적으로 업로드되었습니다.")
                    .success(true)
                    .build();

        } catch (IOException e) {
            log.error("Failed to save video file", e);
            throw new FileProcessingException("영상 파일 저장에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 이미지 파일 저장 및 압축
     */
    public UploadResultDto saveImageFile(MultipartFile file) {
        return saveImageFile(file, true, null, null, fileConfig.getImageQuality());
    }

    public UploadResultDto saveImageFile(MultipartFile file, boolean compress,
                                         Integer targetWidth, Integer targetHeight, Float quality) {
        try {
            validateImageFile(file);

            // 디렉토리 생성
            Path imageDir = createDirectoryIfNotExists(fileConfig.getUploadDir(), fileConfig.getImageDir());

            // 고유한 파일명 생성
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            Path targetPath = imageDir.resolve(fileName);

            if (compress) {
                // 이미지 압축 및 리사이징
                BufferedImage image = ImageIO.read(file.getInputStream());
                BufferedImage processedImage = imageUtils.processImage(image, targetWidth, targetHeight);

                // 압축된 이미지 저장
                String format = FileUtils.getFileExtension(fileName).toLowerCase();
                imageUtils.saveCompressedImage(processedImage, targetPath.toFile(), format,
                        quality != null ? quality : fileConfig.getImageQuality());

                log.info("Compressed image saved: {}", targetPath);
            } else {
                // 원본 이미지 저장
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Original image saved: {}", targetPath);
            }

            return UploadResultDto.builder()
                    .filePath(targetPath.toString())
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(Files.size(targetPath))
                    .contentType(file.getContentType())
                    .message("이미지 파일이 성공적으로 업로드되었습니다.")
                    .success(true)
                    .build();

        } catch (IOException e) {
            log.error("Failed to save image file", e);
            throw new FileProcessingException("이미지 파일 저장에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 영상 썸네일 생성 (0:01초 캡처)
     */
    public String generateVideoThumbnail(String videoPath) {
        try {
            Path thumbnailDir = createDirectoryIfNotExists(fileConfig.getUploadDir(), fileConfig.getThumbnailDir());

            String thumbnailFileName = "thumb_" + UUID.randomUUID() + ".jpg";
            Path thumbnailPath = thumbnailDir.resolve(thumbnailFileName);

            // 1초 지점에서 썸네일 추출
            videoUtils.extractThumbnail(videoPath, thumbnailPath.toString(), 1);

            log.info("Thumbnail generated: {}", thumbnailPath);
            return thumbnailPath.toString();

        } catch (Exception e) {
            log.error("Failed to generate thumbnail for video: {}", videoPath, e);
            return null; // 썸네일 생성 실패는 치명적이지 않음
        }
    }

    /**
     * 영상 메타데이터 추출
     */
    public FileMetadata extractVideoMetadata(String videoPath) {
        return videoUtils.extractMetadata(videoPath);
    }

    /**
     * 이미지 메타데이터 추출
     */
    public FileMetadata extractImageMetadata(String imagePath) {
        return imageUtils.extractMetadata(imagePath);
    }

    /**
     * 파일 삭제
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 파일 크기 조회
     */
    public long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to get file size: {}", filePath, e);
            return 0;
        }
    }

    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("영상 파일이 비어있습니다.");
        }

        if (file.getSize() > fileConfig.getMaxVideoSize()) {
            throw new InvalidFileException("영상 파일 크기가 제한을 초과했습니다. 최대 크기: " +
                    FileUtils.formatFileSize(fileConfig.getMaxVideoSize()));
        }

        String extension = FileUtils.getFileExtension(file.getOriginalFilename());
        if (!isAllowedVideoExtension(extension)) {
            throw new InvalidFileException("지원하지 않는 영상 파일 형식입니다. 지원 형식: " +
                    String.join(", ", fileConfig.getAllowedVideoExtensions()));
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("이미지 파일이 비어있습니다.");
        }

        if (file.getSize() > fileConfig.getMaxImageSize()) {
            throw new InvalidFileException("이미지 파일 크기가 제한을 초과했습니다. 최대 크기: " +
                    FileUtils.formatFileSize(fileConfig.getMaxImageSize()));
        }

        String extension = FileUtils.getFileExtension(file.getOriginalFilename());
        if (!isAllowedImageExtension(extension)) {
            throw new InvalidFileException("지원하지 않는 이미지 파일 형식입니다. 지원 형식: " +
                    String.join(", ", fileConfig.getAllowedImageExtensions()));
        }
    }

    private boolean isAllowedVideoExtension(String extension) {
        for (String allowed : fileConfig.getAllowedVideoExtensions()) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedImageExtension(String extension) {
        for (String allowed : fileConfig.getAllowedImageExtensions()) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    private Path createDirectoryIfNotExists(String... pathSegments) throws IOException {
        Path path = Paths.get(String.join("/", pathSegments));
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = FileUtils.getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return timestamp + "_" + uuid + "." + extension;
    }

    private String generateChecksum(Path filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] digest = md.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            log.warn("Failed to generate checksum for file: {}", filePath, e);
            return null;
        }
    }
}