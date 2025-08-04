package kt.aivle.store.adapter.out.event;

import lombok.Builder;

@Builder
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