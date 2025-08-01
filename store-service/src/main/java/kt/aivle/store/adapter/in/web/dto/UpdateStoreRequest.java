package kt.aivle.store.adapter.in.web.dto;

import kt.aivle.store.domain.model.Industry;

public record UpdateStoreRequest(
        String name,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude,
        Industry industry
) {
}
