package kt.aivle.store.adapter.in.web.dto;

import kt.aivle.store.domain.model.Industry;
import kt.aivle.store.domain.model.Store;
import lombok.Builder;

@Builder
public record StoreResponse(
        Long id,
        Long userId,
        String name,
        String address,
        String phoneNumber,
        String businessNumber,
        Double latitude,
        Double longitude,
        Industry industry
) {
    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .userId(store.getUserId())
                .name(store.getName())
                .address(store.getAddress())
                .phoneNumber(store.getPhoneNumber())
                .businessNumber(store.getBusinessNumber())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .industry(store.getIndustry())
                .build();
    }
}

