package kt.aivle.content.infra;

import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static kt.aivle.common.code.CommonResponseCode.INTERNAL_SERVER_ERROR;
import static kt.aivle.content.exception.ContentErrorCode.IMAGE_UPLOAD_ERROR;

@Component
@RequiredArgsConstructor
public class S3Storage {

    private final S3Client s3;

    @Value("${cloud.aws.s3.bucket.origin}")
    private String originBucket;

    @Value("${cloud.aws.s3.bucket.temp}")
    private String tempBucket;

    public record S3ObjectStat(String contentType, long contentLength) {
    }

    public void put(String key, InputStream in, long contentLength, String contentType, String cacheControl) {
        try {
            PutObjectRequest.Builder req = PutObjectRequest.builder()
                    .bucket(originBucket)
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

    public void delete(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(originBucket).key(key).build());
        } catch (Exception ignore) {
        }
    }

    private String contentType(MultipartFile f) {
        return f.getContentType() == null ? "application/octet-stream" : f.getContentType();
    }

    public S3ObjectStat head(String key) {
        HeadObjectResponse head = s3.headObject(b -> b.bucket(tempBucket).key(key));
        return new S3ObjectStat(head.contentType(), head.contentLength());
    }

    public void copy(String srcKey, String dstKey) {
        s3.copyObject(c -> c
                .copySource(tempBucket + "/" + srcKey)
                .destinationBucket(originBucket)
                .destinationKey(dstKey)
                .metadataDirective(MetadataDirective.COPY)
        );
    }

    public Path downloadToTempFile(String key, String suffix) throws IOException {
        Path tmp = Files.createTempFile("aivle-origin-", suffix);
        try (ResponseInputStream<GetObjectResponse> in = s3.getObject(b -> b.bucket(tempBucket).key(key))) {
            Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return tmp;
    }
}
