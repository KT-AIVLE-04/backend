package kt.aivle.shorts.adapter.out.s3;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.shorts.adapter.out.s3.dto.UploadedImageInfo;
import kt.aivle.shorts.adapter.out.s3.mapper.S3ImageMapper;
import kt.aivle.shorts.application.port.out.s3.DeleteImageRequest;
import kt.aivle.shorts.application.port.out.s3.MediaStoragePort;
import kt.aivle.shorts.application.port.out.s3.UploadImageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static kt.aivle.shorts.exception.ShortsErrorCode.IMAGE_UPLOAD_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3StorageAdapter implements MediaStoragePort {

    private final S3AsyncClient s3AsyncClient;
    private final S3Presigner s3Presigner;
    private final S3ImageMapper mapper;

    private final String bucketName = "aivle-contents";
    private final List<String> allowedTypes = List.of("image/jpeg", "image/png");

    @Value("${cloud.aws.region.static}")
    private String region;


    @Override
    public Mono<List<UploadImageResponse>> uploadImages(List<FilePart> images) {
        if (images == null || images.isEmpty()) {
            throw new BusinessException(IMAGE_UPLOAD_ERROR, "업로드할 이미지가 없습니다.");
        }
        return Flux.fromIterable(images)
                .flatMap(this::uploadSingleImage, 5)
                .map(mapper::toUploadImageResponse)
                .collectList();
    }

    private Mono<UploadedImageInfo> uploadSingleImage(FilePart filePart) {
        String contentType = filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : "";

        if (!allowedTypes.contains(contentType)) {
            return Mono.error(new BusinessException(IMAGE_UPLOAD_ERROR, "허용되지 않은 파일 타입: " + contentType));
        }

        String originalName = filePart.filename();
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
                .map(resp -> {
                    String url = makeFileUrl(key);
                    String presignedUrl = presignGetUrl(key, Duration.ofMinutes(15));
                    return UploadedImageInfo.from(
                            url,
                            presignedUrl,
                            key,
                            originalName,
                            contentType
                    );
                })
                .onErrorMap(SdkException.class, e -> new BusinessException(IMAGE_UPLOAD_ERROR, e.getMessage()));
    }

    private String presignGetUrl(String key, Duration ttl) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(getReq)
                .build();

        return s3Presigner.presignGetObject(presignReq).url().toString();
    }

    private String makeFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    @Override
    public Mono<Void> deleteImages(List<DeleteImageRequest> requests) {
        return Flux.fromIterable(requests)
                .map(mapper::toDeleteS3ImageRequest)
                .flatMap(img ->
                        Mono.fromFuture(s3AsyncClient.deleteObject(
                                DeleteObjectRequest.builder()
                                        .bucket(bucketName)
                                        .key(img.s3Key())
                                        .build()
                        )).onErrorResume(e -> {
                            log.warn("S3 이미지 삭제 실패: " + img.s3Key(), e);
                            return Mono.empty();
                        })
                ).then();
    }
}