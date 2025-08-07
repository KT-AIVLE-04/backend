package kt.aivle.shorts.application.port.out;

import kt.aivle.shorts.adapter.out.s3.UploadedImageInfo;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ImageStoragePort {
    Mono<List<UploadedImageInfo>> uploadImages(List<FilePart> images);

    Mono<Void> deleteImages(List<UploadedImageInfo> uploadedImageInfos);
}
