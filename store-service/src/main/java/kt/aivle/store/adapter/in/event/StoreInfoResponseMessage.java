package kt.aivle.store.adapter.in.event;

import lombok.Builder;

@Builder
public record StoreInfoResponseMessage(
        Long storeId,
        String name,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude,
        String industry,
        String requestId) {
}


