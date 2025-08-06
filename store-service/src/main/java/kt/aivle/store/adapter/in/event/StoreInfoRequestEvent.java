package kt.aivle.store.adapter.in.event;

public record StoreInfoRequestEvent(String requestId, Long storeId, Long userId) {
}
