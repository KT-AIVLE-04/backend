package kt.aivle.store.application.port.in;

import kt.aivle.store.adapter.in.event.StoreInfoRequestMessage;
import kt.aivle.store.adapter.in.event.StoreInfoResponseMessage;

public interface StoreEventUseCase {
    StoreInfoResponseMessage buildResponse(StoreInfoRequestMessage event);
}
