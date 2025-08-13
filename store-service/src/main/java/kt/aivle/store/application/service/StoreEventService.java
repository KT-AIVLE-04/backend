package kt.aivle.store.application.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.store.adapter.in.event.StoreInfoRequestMessage;
import kt.aivle.store.adapter.in.event.StoreInfoResponseMessage;
import kt.aivle.store.application.port.in.StoreEventUseCase;
import kt.aivle.store.application.port.out.StoreRepositoryPort;
import kt.aivle.store.domain.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static kt.aivle.store.exception.StoreErrorCode.NOT_AUTHORITY;
import static kt.aivle.store.exception.StoreErrorCode.NOT_FOUND_STORE;

@Service
@RequiredArgsConstructor
public class StoreEventService implements StoreEventUseCase {

    private final StoreRepositoryPort storeRepositoryPort;

    @Override
    public StoreInfoResponseMessage buildResponse(StoreInfoRequestMessage req) {
        Store store = storeRepositoryPort.findById(req.storeId())
                .orElseThrow(() -> new BusinessException(NOT_FOUND_STORE));

        if (!store.getUserId().equals(req.userId())) {
            throw new BusinessException(NOT_AUTHORITY);
        }

        return StoreInfoResponseMessage.builder()
                .storeId(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phoneNumber(store.getPhoneNumber())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .industry(store.getIndustry())
                .build();
    }
}