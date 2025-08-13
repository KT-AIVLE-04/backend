package kt.aivle.shorts.adapter.out.event.store.dto;

public record StoreInfoResponseMessage(
        Long storeId,
        String name,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude,
        String industry
) {
}