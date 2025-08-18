package kt.aivle.shorts.adapter.out.s3;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.shorts.adapter.out.s3.dto.UploadedObject;
import kt.aivle.shorts.adapter.out.s3.mapper.S3ImageMapper;
import kt.aivle.shorts.application.port.out.s3.MediaStoragePort;
import kt.aivle.shorts.application.port.out.s3.UploadedObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URLEncoder;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static kt.aivle.shorts.exception.ShortsErrorCode.IMAGE_UPLOAD_ERROR;

@Component
@RequiredArgsConstructor
public class S3StorageAdapter implements MediaStoragePort {

    private final S3AsyncClient s3AsyncClient;
    private final S3Presigner s3Presigner;
    private final S3ImageMapper mapper;

    private static final String BUCKET_NAME = "aivle-temp";
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png");

    private static final Duration DEFAULT_PRESIGN_TTL = Duration.ofMinutes(15);

    @Value("${cloud.aws.region.static}")
    private String region;

    @Override
    public Mono<UploadedObjectResponse> uploadTempImage(FilePart image) {
        if (image == null) {
            return Mono.error(new BusinessException(IMAGE_UPLOAD_ERROR, "업로드할 이미지가 없습니다."));
        }

        final String contentType = image.headers().getContentType() != null
                ? image.headers().getContentType().toString()
                : "";

        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return Mono.error(new BusinessException(IMAGE_UPLOAD_ERROR, "허용되지 않은 파일 타입: " + contentType));
        }

        final String originalName = image.filename();
        final String key = UUID.randomUUID() + "-" + urlEncode(originalName);

        return writeToTempFile(image.content(), "img-")
                .flatMap(temp ->
                        fileSize(temp)
                                .flatMap(size -> putFromFile(temp, key, contentType, size)
                                        .doFinally(sig -> deleteFile(temp))
                                )
                )
                .then(Mono.fromCallable(() -> toResponse(key, originalName, contentType)))
                .onErrorMap(SdkException.class, e -> new BusinessException(IMAGE_UPLOAD_ERROR, e.getMessage()));
    }

    private Mono<Path> writeToTempFile(Flux<DataBuffer> content, String prefix) {
        return Mono.fromCallable(() -> {
                    Path temp = Files.createTempFile(prefix, ".upload");
                    AsynchronousFileChannel ch = AsynchronousFileChannel.open(
                            temp,
                            EnumSet.of(StandardOpenOption.WRITE),
                            java.util.concurrent.Executors.newCachedThreadPool()
                    );
                    return new TempFile(ch, temp);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tf ->
                        DataBufferUtils.write(content, tf.channel, 0)
                                .then(Mono.fromCallable(() -> {
                                    tf.channel.close();
                                    return tf.path;
                                }).subscribeOn(Schedulers.boundedElastic()))
                                .onErrorResume(err -> {
                                    safeClose(tf.channel);
                                    deleteFile(tf.path);
                                    return Mono.error(err);
                                })
                );
    }

    private Mono<Long> fileSize(Path path) {
        return Mono.fromCallable(() -> Files.size(path))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Void> putFromFile(Path path, String key, String contentType, long size) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();

        return Mono.fromFuture(s3AsyncClient.putObject(req, AsyncRequestBody.fromFile(path))).then();
    }

    private String getPresignUrl(String key) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(DEFAULT_PRESIGN_TTL)
                .getObjectRequest(getReq)
                .build();

        return s3Presigner.presignGetObject(presignReq).url().toString();
    }

    private String makeFileUrl(String key) {
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(BUCKET_NAME, region, key);
    }

    private UploadedObjectResponse toResponse(String key, String originalName, String contentType) {
        String url = makeFileUrl(key);
        String presignedUrl = getPresignUrl(key);
        UploadedObject uo = UploadedObject.from(url, presignedUrl, key, originalName, contentType);
        return mapper.toUploadImageResponse(uo);
    }

    private String urlEncode(String name) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    private void deleteFile(Path p) {
        Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(p);
            } catch (Exception ignore) {
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    private void safeClose(AsynchronousFileChannel ch) {
        try {
            ch.close();
        } catch (Exception ignore) {
        }
    }

    private record TempFile(AsynchronousFileChannel channel, Path path) {
    }
}