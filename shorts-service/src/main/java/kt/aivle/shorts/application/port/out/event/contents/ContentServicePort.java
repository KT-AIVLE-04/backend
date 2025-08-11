package kt.aivle.shorts.application.port.out.event.contents;

import reactor.core.publisher.Mono;

public interface ContentServicePort {
    Mono<Void> createContent(CreateContentRequest request);
}

