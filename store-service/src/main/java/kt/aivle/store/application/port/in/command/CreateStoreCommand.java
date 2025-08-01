package kt.aivle.store.application.port.in.command;

import kt.aivle.store.domain.model.Industry;

public record CreateStoreCommand(
        Long userId,
        String name,
        String address,
        String phoneNumber,
        String businessNumber,
        Double latitude,
        Double longitude,
        Industry industry
) {}
