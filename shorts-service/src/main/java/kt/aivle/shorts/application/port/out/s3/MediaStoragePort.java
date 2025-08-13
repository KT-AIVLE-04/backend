package kt.aivle.shorts.application.port.out.s3;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface MediaStoragePort {
    Mono<UploadedObjectResponse> uploadTempImage(FilePart image);

    Mono<UploadedObjectResponse> uploadVideoFromUrl(String sourceUrl);
}