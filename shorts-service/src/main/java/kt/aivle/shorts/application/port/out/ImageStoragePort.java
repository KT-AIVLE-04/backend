package kt.aivle.shorts.application.port.out;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ImageStoragePort {
    Mono<List<String>> uploadImages(List<FilePart> images);
}
