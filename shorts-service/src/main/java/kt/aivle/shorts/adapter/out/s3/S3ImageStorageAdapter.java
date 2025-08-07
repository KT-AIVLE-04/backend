package kt.aivle.shorts.adapter.out.s3;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.shorts.application.port.out.ImageStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static kt.aivle.shorts.exception.ShortsErrorCode.IMAGE_UPLOAD_ERROR;

@Component
@RequiredArgsConstructor
public class S3ImageStorageAdapter implements ImageStoragePort {

    private final S3AsyncClient s3AsyncClient;

    private final String bucketName = "aivle-contents";
    private final List<String> allowedTypes = List.of("image/jpeg", "image/png");

    @Value("${cloud.aws.region.static}")
    private String region;

    @Override
    public Mono<List<String>> uploadImages(List<FilePart> images) {
        if (images == null || images.isEmpty()) {
            throw new BusinessException(IMAGE_UPLOAD_ERROR, "업로드할 이미지가 없습니다.");
        }
        return Flux.fromIterable(images)
                .flatMap(this::uploadSingleImage, 5)
                .collectList();
    }

    private Mono<String> uploadSingleImage(FilePart filePart) {
        String contentType = filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : "";

        if (!allowedTypes.contains(contentType)) {
            return Mono.error(new BusinessException(IMAGE_UPLOAD_ERROR, "허용되지 않은 파일 타입: " + contentType));
        }

        String key = "images/" + UUID.randomUUID() + "-" + URLEncoder.encode(filePart.filename(), StandardCharsets.UTF_8);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    return Mono.fromFuture(
                            s3AsyncClient.putObject(request, AsyncRequestBody.fromBytes(bytes))
                    );
                })
                .map(response -> makeFileUrl(key))
                .onErrorMap(SdkException.class, e -> new BusinessException(IMAGE_UPLOAD_ERROR, e.getMessage()));
    }

    private String makeFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
}