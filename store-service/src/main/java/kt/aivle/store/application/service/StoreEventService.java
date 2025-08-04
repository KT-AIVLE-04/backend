package kt.aivle.store.application.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.store.adapter.in.event.StoreInfoRequestEvent;
import kt.aivle.store.adapter.out.event.StoreInfoEventProducer;
import kt.aivle.store.adapter.out.event.StoreInfoResponseEvent;
import kt.aivle.store.application.port.in.StoreEventUseCase;
import kt.aivle.store.application.port.out.StoreRepositoryPort;
import kt.aivle.store.domain.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static kt.aivle.store.exception.StoreErrorCode.NOT_FOUND_STORE;

@Service
@RequiredArgsConstructor
public class StoreEventService implements StoreEventUseCase {

    private final StoreRepositoryPort storeRepositoryPort;
    private final StoreInfoEventProducer eventProducer;

    @Override
    public void handleStoreInfoRequest(StoreInfoRequestEvent event) {
        Store store = storeRepositoryPort.findById(event.storeId())
                .orElseThrow(() -> new BusinessException(NOT_FOUND_STORE));

        StoreInfoResponseEvent responseEvent = StoreInfoResponseEvent.builder()
                .storeId(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phoneNumber(store.getPhoneNumber())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .industry(store.getIndustry().name())
                .requestId(event.requestId())
                .build();
        eventProducer.send(responseEvent);
    }
}

