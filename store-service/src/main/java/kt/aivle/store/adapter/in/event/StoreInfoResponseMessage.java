package kt.aivle.store.adapter.in.event;

import kt.aivle.store.domain.model.Industry;
import lombok.Builder;

@Builder
public record StoreInfoResponseMessage(
        Long storeId,
        String name,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude,
        Industry industry
) {
}