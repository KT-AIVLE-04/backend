package kt.aivle.shortsmvc.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public String uploadTempImage(MultipartFile file) {
        try {
            String key = "temp/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            
            S3Client s3Client = createS3Client();
            
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
            
            // 임시 presigned URL 반환 (실제로는 바로 S3 URL을 반환할 수도 있음)
            return getPresignedUrl(key);
            
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    public String getPresignedUrl(String key) {
        try {
            S3Presigner presigner = createS3Presigner();
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return presigner.presignGetObject(presignRequest).url().toString();
            
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key: {}", key, e);
            return null;
        }
    }

    private S3Client createS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private S3Presigner createS3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}