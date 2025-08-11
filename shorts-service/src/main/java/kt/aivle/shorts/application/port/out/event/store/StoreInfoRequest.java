package kt.aivle.shorts.application.port.out.event.store;

public record StoreInfoRequest(Long storeId, Long userId, String correlationId) {
}
