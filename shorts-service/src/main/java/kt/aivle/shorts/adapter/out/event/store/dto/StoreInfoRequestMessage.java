package kt.aivle.shorts.adapter.out.event.store.dto;

public record StoreInfoRequestMessage(String requestId, Long storeId, Long userId) {
}