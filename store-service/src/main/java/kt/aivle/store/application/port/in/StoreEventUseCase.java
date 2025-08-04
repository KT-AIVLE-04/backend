package kt.aivle.store.application.port.in;

import kt.aivle.store.adapter.in.event.StoreInfoRequestEvent;

public interface StoreEventUseCase {
    void handleStoreInfoRequest(StoreInfoRequestEvent event);
}
