package kt.aivle.sns.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class S3Storage {

    private final S3Client s3;

    @Value("${cloud.aws.s3.bucket.origin}")
    private String originBucket;

    private final S3Presigner s3Presigner;

    private static final Duration DEFAULT_PRESIGN_TTL = Duration.ofMinutes(15);

    public String getPresignUrl(String key) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(originBucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(DEFAULT_PRESIGN_TTL)
                .getObjectRequest(getReq)
                .build();

        return s3Presigner.presignGetObject(presignReq).url().toString();
    }

    /**
     * S3 객체 메타정보 조회 (Content-Length, Content-Type 등)
     */
    public HeadObjectResponse head(String key) {
        if (key == null || key.isBlank()) {
            throw S3Exception.builder().message("S3 key is blank").build();
        }
        return s3.headObject(HeadObjectRequest.builder()
                .bucket(originBucket)
                .key(key)
                .build());
    }

    /**
     * S3 객체 스트림 열기 (반드시 호출 측에서 close 필요)
     */
    public ResponseInputStream<GetObjectResponse> openStream(String key) {
        if (key == null || key.isBlank()) {
            throw S3Exception.builder().message("S3 key is blank").build();
        }
        return s3.getObject(GetObjectRequest.builder()
                .bucket(originBucket)
                .key(key)
                .build());
    }

    /**
     * 유튜브 업로드용: 스트림 + Content-Length + Content-Type 한 번에 가져오기.
     * try-with-resources 로 닫아주세요.
     * <p>
     * 사용 예)
     * try (var s = s3Storage.fetchForUpload(objectKey)) {
     * InputStreamContent media = new InputStreamContent(s.contentTypeOrDefault(), s.stream());
     * media.setLength(s.contentLength());
     * // ... YouTube Insert 호출
     * }
     */
    public S3ObjectStream fetchForUpload(String key) {
        HeadObjectResponse head = head(key);
        ResponseInputStream<GetObjectResponse> stream = openStream(key);
        return new S3ObjectStream(stream, head.contentLength(), head.contentType());
    }

    /**
     * S3 객체 스트림 + 메타정보를 담는 핸들.
     * AutoCloseable 이라 try-with-resources 가능.
     */
    public static final class S3ObjectStream implements AutoCloseable {
        private final ResponseInputStream<GetObjectResponse> stream;
        private final long contentLength;
        private final String contentType;

        public S3ObjectStream(ResponseInputStream<GetObjectResponse> stream,
                              long contentLength,
                              String contentType) {
            this.stream = stream;
            this.contentLength = contentLength;
            this.contentType = contentType;
        }

        public ResponseInputStream<GetObjectResponse> stream() {
            return stream;
        }

        public long contentLength() {
            return contentLength;
        }

        public String contentType() {
            return contentType;
        }

        @Override
        public void close() throws IOException {
            if (stream != null) {
                stream.close();
            }
        }
    }
}