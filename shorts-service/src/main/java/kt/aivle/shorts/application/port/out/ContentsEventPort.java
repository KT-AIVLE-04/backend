package kt.aivle.shorts.application.port.out;

import kt.aivle.shorts.adapter.out.event.CreateContentRequestEvent;
import reactor.core.publisher.Mono;

public interface ContentsEventPort {
    Mono<Void> publish(CreateContentRequestEvent event);
}
