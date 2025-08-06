package kt.aivle.content.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.base-url}")
    private String baseUrl;

    @Value("${media.storage.video-path}")
    private String videoPath;

    @Value("${media.storage.image-path}")
    private String imagePath;

    @Value("${media.storage.thumbnail-path}")
    private String thumbnailPath;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * 비디오 파일 업로드
     */
    public S3UploadResult uploadVideo(MultipartFile file, Long userId) {
        String key = generateVideoKey(file.getOriginalFilename(), userId);
        return uploadFile(file, key, "video");
    }

    /**
     * 이미지 파일 업로드
     */
    public S3UploadResult uploadImage(MultipartFile file, Long userId) {
        String key = generateImageKey(file.getOriginalFilename(), userId);
        return uploadFile(file, key, "image");
    }

    /**
     * 썸네일 파일 업로드
     */
    public S3UploadResult uploadThumbnail(byte[] thumbnailData, String originalFileName, Long userId, String mediaType) {
        String key = generateThumbnailKey(originalFileName, userId, mediaType);
        return uploadFile(thumbnailData, key, "image/jpeg");
    }

    /**
     * 파일 삭제
     */
    public boolean deleteFile(String fileKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            logger.info("S3 파일 삭제 성공: {}", fileKey);
            return true;

        } catch (Exception e) {
            logger.error("S3 파일 삭제 실패: {}", fileKey, e);
            return false;
        }
    }

    /**
     * 여러 파일 일괄 삭제
     */
    public boolean deleteFiles(String... fileKeys) {
        try {
            // 삭제할 객체 목록 생성
            ObjectIdentifier[] objects = new ObjectIdentifier[fileKeys.length];
            for (int i = 0; i < fileKeys.length; i++) {
                objects[i] = ObjectIdentifier.builder().key(fileKeys[i]).build();
            }

            Delete delete = Delete.builder()
                    .objects(objects)
                    .build();

            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(delete)
                    .build();

            DeleteObjectsResponse response = s3Client.deleteObjects(deleteRequest);

            logger.info("S3 파일 일괄 삭제 성공: {} 개 파일", response.deleted().size());
            return response.errors().isEmpty();

        } catch (Exception e) {
            logger.error("S3 파일 일괄 삭제 실패", e);
            return false;
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean fileExists(String fileKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("S3 파일 존재 확인 실패: {}", fileKey, e);
            return false;
        }
    }

    /**
     * 파일 크기 조회
     */
    public long getFileSize(String fileKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);
            return response.contentLength();

        } catch (Exception e) {
            logger.error("S3 파일 크기 조회 실패: {}", fileKey, e);
            return 0;
        }
    }

    // === Private Helper Methods ===

    /**
     * MultipartFile 업로드
     */
    private S3UploadResult uploadFile(MultipartFile file, String key, String mediaType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .metadata(java.util.Map.of(
                            "original-filename", file.getOriginalFilename(),
                            "media-type", mediaType,
                            "upload-time", LocalDateTime.now().toString()
                    ))
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = baseUrl + "/" + key;

            logger.info("S3 파일 업로드 성공: {} -> {}", file.getOriginalFilename(), key);

            return S3UploadResult.success(key, fileUrl, file.getSize());

        } catch (IOException e) {
            logger.error("파일 읽기 실패: {}", file.getOriginalFilename(), e);
            return S3UploadResult.failure("파일 읽기 실패: " + e.getMessage());
        } catch (Exception e) {
            logger.error("S3 업로드 실패: {}", file.getOriginalFilename(), e);
            return S3UploadResult.failure("S3 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * byte[] 업로드 (썸네일용)
     */
    private S3UploadResult uploadFile(byte[] data, String key, String contentType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) data.length)
                    .metadata(java.util.Map.of(
                            "media-type", "thumbnail",
                            "upload-time", LocalDateTime.now().toString()
                    ))
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(data));

            String fileUrl = baseUrl + "/" + key;

            logger.info("S3 썸네일 업로드 성공: {}", key);

            return S3UploadResult.success(key, fileUrl, (long) data.length);

        } catch (Exception e) {
            logger.error("S3 썸네일 업로드 실패: {}", key, e);
            return S3UploadResult.failure("S3 썸네일 업로드 실패: " + e.getMessage());
        }
    }

    /**
     * 비디오 파일 키 생성
     */
    private String generateVideoKey(String originalFilename, Long userId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);

        return String.format("%suser_%d/%s_%s.%s", videoPath, userId, timestamp, uuid, extension);
    }

    /**
     * 이미지 파일 키 생성
     */
    private String generateImageKey(String originalFilename, Long userId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);

        return String.format("%suser_%d/%s_%s.%s", imagePath, userId, timestamp, uuid, extension);
    }

    /**
     * 썸네일 파일 키 생성
     */
    private String generateThumbnailKey(String originalFilename, Long userId, String mediaType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s%s/user_%d/%s_%s_thumb.jpg", thumbnailPath, mediaType, userId, timestamp, uuid);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    // === 결과 클래스 ===

    public static class S3UploadResult {
        private final boolean success;
        private final String fileKey;
        private final String fileUrl;
        private final Long fileSize;
        private final String errorMessage;

        private S3UploadResult(boolean success, String fileKey, String fileUrl, Long fileSize, String errorMessage) {
            this.success = success;
            this.fileKey = fileKey;
            this.fileUrl = fileUrl;
            this.fileSize = fileSize;
            this.errorMessage = errorMessage;
        }

        public static S3UploadResult success(String fileKey, String fileUrl, Long fileSize) {
            return new S3UploadResult(true, fileKey, fileUrl, fileSize, null);
        }

        public static S3UploadResult failure(String errorMessage) {
            return new S3UploadResult(false, null, null, null, errorMessage);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getFileKey() { return fileKey; }
        public String getFileUrl() { return fileUrl; }
        public Long getFileSize() { return fileSize; }
        public String getErrorMessage() { return errorMessage; }
    }
}