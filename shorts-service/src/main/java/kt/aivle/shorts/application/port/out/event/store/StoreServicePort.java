package kt.aivle.shorts.application.port.out.event.store;

import reactor.core.publisher.Mono;

public interface StoreServicePort {
    Mono<StoreInfoResponse> getStoreInfo(StoreInfoRequest request);
}
