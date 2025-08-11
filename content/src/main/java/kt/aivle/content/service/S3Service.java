package kt.aivle.content.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    /**
     * 파일을 S3에 업로드하고 URL 반환
     */
    public S3UploadResult uploadFile(MultipartFile file, String folder) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String s3Key = generateS3Key(folder, extension);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            metadata.setCacheControl("max-age=31536000"); // 1년 캐시

            PutObjectRequest putRequest = new PutObjectRequest(
                    bucketName,
                    s3Key,
                    file.getInputStream(),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3.putObject(putRequest);

            String s3Url = generateS3Url(s3Key);

            return new S3UploadResult(s3Url, s3Key, file.getSize());

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * InputStream을 사용하여 파일 업로드 (썸네일 생성 시 사용)
     */
    public S3UploadResult uploadInputStream(InputStream inputStream, String contentType,
                                            long contentLength, String folder, String filename) {
        try {
            String s3Key = folder + "/" + filename;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(contentLength);
            metadata.setCacheControl("max-age=31536000");

            PutObjectRequest putRequest = new PutObjectRequest(
                    bucketName,
                    s3Key,
                    inputStream,
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3.putObject(putRequest);

            String s3Url = generateS3Url(s3Key);

            return new S3UploadResult(s3Url, s3Key, contentLength);

        } catch (Exception e) {
            throw new RuntimeException("InputStream 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * S3에서 파일 삭제
     */
    public void deleteFile(String s3Key) {
        try {
            if (doesObjectExist(s3Key)) {
                amazonS3.deleteObject(bucketName, s3Key);
            }
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 여러 파일 일괄 삭제
     */
    public void deleteFiles(String... s3Keys) {
        for (String s3Key : s3Keys) {
            if (s3Key != null && !s3Key.trim().isEmpty()) {
                deleteFile(s3Key);
            }
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean doesObjectExist(String s3Key) {
        try {
            return amazonS3.doesObjectExist(bucketName, s3Key);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 파일 메타데이터 조회
     */
    public ObjectMetadata getFileMetadata(String s3Key) {
        try {
            return amazonS3.getObjectMetadata(bucketName, s3Key);
        } catch (Exception e) {
            throw new RuntimeException("파일 메타데이터 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Presigned URL 생성 (임시 접근 URL)
     */
    public String generatePresignedUrl(String s3Key, int expirationMinutes) {
        try {
            java.util.Date expiration = new java.util.Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * expirationMinutes;
            expiration.setTime(expTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, s3Key)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();

        } catch (Exception e) {
            throw new RuntimeException("Presigned URL 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // === Private Helper Methods ===

    /**
     * S3 키 생성 (폴더/UUID.확장자)
     */
    private String generateS3Key(String folder, String extension) {
        return folder + "/" + UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * S3 URL 생성
     */
    private String generateS3Url(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    // === Inner Classes ===

    /**
     * S3 업로드 결과 클래스
     */
    public static class S3UploadResult {
        private final String s3Url;
        private final String s3Key;
        private final long fileSize;

        public S3UploadResult(String s3Url, String s3Key, long fileSize) {
            this.s3Url = s3Url;
            this.s3Key = s3Key;
            this.fileSize = fileSize;
        }

        public String getS3Url() {
            return s3Url;
        }

        public String getS3Key() {
            return s3Key;
        }

        public long getFileSize() {
            return fileSize;
        }
    }

    // === Constants ===

    public static final String FOLDER_IMAGES = "images";
    public static final String FOLDER_VIDEOS = "videos";
    public static final String FOLDER_THUMBNAILS = "thumbnails";
}