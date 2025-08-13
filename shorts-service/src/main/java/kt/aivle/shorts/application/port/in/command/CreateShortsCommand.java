package kt.aivle.shorts.application.port.in.command;


import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

public record CreateShortsCommand(
        String sessionId,
        String title,
        String content,
        Integer adDuration,
        Flux<FilePart> images
) {
}

