package kt.aivle.content.infra;

import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;

import static kt.aivle.common.code.CommonResponseCode.INTERNAL_SERVER_ERROR;
import static kt.aivle.content.exception.ContentErrorCode.IMAGE_UPLOAD_ERROR;

@Component
@RequiredArgsConstructor
public class S3Storage {

    private final S3Client s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void put(String key, InputStream in, long contentLength, String contentType, String cacheControl) {
        try {
            PutObjectRequest.Builder req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentLength(contentLength)
                    .contentType(contentType);
            if (cacheControl != null) req.cacheControl(cacheControl);

            s3.putObject(req.build(), RequestBody.fromInputStream(in, contentLength));
        } catch (S3Exception e) {
            throw new BusinessException(IMAGE_UPLOAD_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(INTERNAL_SERVER_ERROR, "S3 업로드 실패: " + e.getMessage());
        }
    }

    public void put(String key, MultipartFile file, String cacheControl) {
        try (InputStream in = file.getInputStream()) {
            put(key, in, file.getSize(), contentType(file), cacheControl);
        } catch (IOException e) {
            throw new BusinessException(IMAGE_UPLOAD_ERROR, e.getMessage());
        }
    }

    public void deleteQuietly(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception ignore) {
        }
    }

    public boolean exists(String key) {
        try {
            s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    private String contentType(MultipartFile f) {
        return f.getContentType() == null ? "application/octet-stream" : f.getContentType();
    }
}
