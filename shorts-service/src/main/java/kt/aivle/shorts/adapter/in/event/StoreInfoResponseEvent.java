package kt.aivle.shorts.adapter.in.event;

public record StoreInfoResponseEvent(
        String requestId,
        Long storeId,
        String name,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude,
        String industry
) {
}