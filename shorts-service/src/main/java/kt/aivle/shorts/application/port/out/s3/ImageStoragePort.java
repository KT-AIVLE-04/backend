package kt.aivle.shorts.application.port.out.s3;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ImageStoragePort {
    Mono<List<UploadImageResponse>> uploadImages(List<FilePart> images);

    Mono<Void> deleteImages(List<DeleteImageRequest> requests);
}
