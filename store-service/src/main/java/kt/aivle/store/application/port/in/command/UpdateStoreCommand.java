package kt.aivle.store.application.port.in.command;

import kt.aivle.store.domain.model.Industry;

public record UpdateStoreCommand(
        Long id,
        Long userId,
        String name,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude,
        Industry industry
) {
}
