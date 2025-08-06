package kt.aivle.shorts.application.port.out;

import kt.aivle.shorts.adapter.in.event.StoreInfoResponseEvent;
import reactor.core.publisher.Mono;

public interface StoreInfoQueryPort {
    Mono<StoreInfoResponseEvent> getStoreInfo(Long storeId, Long userId);
}
