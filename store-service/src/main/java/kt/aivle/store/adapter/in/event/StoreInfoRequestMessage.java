package kt.aivle.store.adapter.in.event;

public record StoreInfoRequestMessage(String requestId, Long storeId, Long userId) {
}
