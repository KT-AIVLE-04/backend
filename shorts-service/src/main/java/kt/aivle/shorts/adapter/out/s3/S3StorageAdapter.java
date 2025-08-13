package kt.aivle.shorts.adapter.out.s3;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.shorts.adapter.out.s3.dto.UploadedObject;
import kt.aivle.shorts.adapter.out.s3.mapper.S3ImageMapper;
import kt.aivle.shorts.application.port.out.s3.MediaStoragePort;
import kt.aivle.shorts.application.port.out.s3.UploadedObjectResponse;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
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
import static kt.aivle.shorts.exception.ShortsErrorCode.WEB_CLIENT_ERROR;

@Component
public class S3StorageAdapter implements MediaStoragePort {

    private final S3AsyncClient s3AsyncClient;
    private final S3Presigner s3Presigner;
    private final S3ImageMapper mapper;
    private final WebClient downloadWebClient;

    public S3StorageAdapter(S3AsyncClient s3AsyncClient,
                            S3Presigner s3Presigner,
                            S3ImageMapper mapper,
                            @Qualifier("downloadWebClient") WebClient downloadWebClient) {
        this.s3AsyncClient = s3AsyncClient;
        this.s3Presigner = s3Presigner;
        this.mapper = mapper;
        this.downloadWebClient = downloadWebClient;
    }

    private static final String BUCKET_NAME = "aivle-contents";
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png");
    private static final List<String> ALLOWED_VIDEO_TYPES = List.of("video/mp4", "video/quicktime", "video/x-matroska", "video/webm");

    private static final String RAW_TAG_HEADER = "purpose=shorts-source";
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
                        fileSize(temp).flatMap(size -> putFromFile(temp, key, contentType, encodeTag(), size)
                                .doFinally(sig -> deleteFile(temp))
                        )
                )
                .then(Mono.fromCallable(() -> toResponse(key, originalName, contentType)))
                .onErrorMap(SdkException.class, e -> new BusinessException(IMAGE_UPLOAD_ERROR, e.getMessage()));
    }

    @Override
    public Mono<UploadedObjectResponse> uploadVideoFromUrl(String sourceUrl) {
        return downloadWebClient.get()
                .uri(sourceUrl)
                .exchangeToMono(res -> {
                    if (!res.statusCode().is2xxSuccessful()) {
                        return Mono.error(new BusinessException(WEB_CLIENT_ERROR, "원본 URL 응답 오류: " + res.statusCode()));
                    }

                    HttpHeaders headers = res.headers().asHttpHeaders();
                    String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
                    if (!StringUtils.hasText(contentType)) contentType = "application/octet-stream";
                    if (ALLOWED_VIDEO_TYPES.stream().noneMatch(contentType::startsWith)) {
                        return Mono.error(new BusinessException(WEB_CLIENT_ERROR, "허용되지 않은 비디오 타입: " + contentType));
                    }

                    String originalName = extractFileName(headers, sourceUrl);
                    String key = UUID.randomUUID() + "-" + urlEncode(originalName);

                    String lenStr = headers.getFirst(HttpHeaders.CONTENT_LENGTH);
                    Flux<DataBuffer> bodyFlux = res.bodyToFlux(DataBuffer.class);

                    if (StringUtils.hasText(lenStr)) {
                        long length = parseLongSafely(lenStr);
                        if (length > 0) {
                            PutObjectRequest req = PutObjectRequest.builder()
                                    .bucket(BUCKET_NAME)
                                    .key(key)
                                    .contentType(contentType)
                                    .contentLength(length)
                                    .build();

                            Publisher<ByteBuffer> body = toByteBufferPublisher(bodyFlux);
                            return Mono.fromFuture(
                                    s3AsyncClient.putObject(req, AsyncRequestBody.fromPublisher(body))
                            ).thenReturn(toResponse(key, originalName, contentType));
                        }
                    }

                    String finalContentType = contentType;
                    return writeToTempFile(bodyFlux, "vid-")
                            .flatMap(temp ->
                                    fileSize(temp).flatMap(size ->
                                            putFromFile(temp, key, finalContentType, null, size)
                                                    .doFinally(sig -> deleteFile(temp))
                                    )
                            )
                            .thenReturn(toResponse(key, originalName, contentType));
                })
                .onErrorMap(e -> new BusinessException(WEB_CLIENT_ERROR, e.getMessage()));
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
                        DataBufferUtils.write(content.map(this::toWritableBuffer), tf.channel, 0)
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

    private DataBuffer toWritableBuffer(DataBuffer db) {
        try {
            byte[] bytes = new byte[db.readableByteCount()];
            db.read(bytes);
            return db.factory().wrap(bytes);
        } finally {
            DataBufferUtils.release(db);
        }
    }

    private Mono<Long> fileSize(Path path) {
        return Mono.fromCallable(() -> Files.size(path))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Void> putFromFile(Path path, String key, String contentType, String tagging, long size) {
        PutObjectRequest.Builder b = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .contentType(contentType)
                .contentLength(size);
        if (StringUtils.hasText(tagging)) {
            b.tagging(tagging);
        }
        PutObjectRequest req = b.build();
        return Mono.fromFuture(s3AsyncClient.putObject(req, AsyncRequestBody.fromFile(path))).then();
    }

    private Publisher<ByteBuffer> toByteBufferPublisher(Flux<DataBuffer> dataBuffers) {
        return dataBuffers.map(db -> {
            try {
                byte[] chunk = new byte[db.readableByteCount()];
                db.read(chunk);
                return ByteBuffer.wrap(chunk);
            } finally {
                DataBufferUtils.release(db);
            }
        });
    }

    private String extractFileName(HttpHeaders headers, String fallbackUrl) {
        String cd = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (StringUtils.hasText(cd)) {
            for (String part : cd.split(";")) {
                String p = part.trim();
                if (p.startsWith("filename=")) {
                    String fn = p.substring("filename=".length()).replace("\"", "");
                    if (StringUtils.hasText(fn)) return fn;
                }
            }
        }
        try {
            String path = URI.create(fallbackUrl).getPath();
            String last = path.substring(path.lastIndexOf('/') + 1);
            if (StringUtils.hasText(last)) return last;
        } catch (Exception ignore) {
        }
        return "video.mp4";
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

    private String encodeTag() {
        return URLEncoder.encode(S3StorageAdapter.RAW_TAG_HEADER, StandardCharsets.UTF_8);
    }

    private long parseLongSafely(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return -1L;
        }
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