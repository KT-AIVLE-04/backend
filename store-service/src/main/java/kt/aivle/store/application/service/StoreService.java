package kt.aivle.store.application.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.store.adapter.in.web.dto.StoreResponse;
import kt.aivle.store.application.port.in.StoreUseCase;
import kt.aivle.store.application.port.in.command.CreateStoreCommand;
import kt.aivle.store.application.port.in.command.DeleteStoreCommand;
import kt.aivle.store.application.port.in.command.UpdateStoreCommand;
import kt.aivle.store.application.port.in.query.GetStoreQuery;
import kt.aivle.store.application.port.in.query.GetStoresQuery;
import kt.aivle.store.application.port.out.StoreRepositoryPort;
import kt.aivle.store.domain.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static kt.aivle.store.exception.StoreErrorCode.NOT_AUTHORITY;
import static kt.aivle.store.exception.StoreErrorCode.NOT_FOUND_STORE;

@Service
@RequiredArgsConstructor
public class StoreService implements StoreUseCase {

    private final StoreRepositoryPort storeRepositoryPort;

    @Transactional(readOnly = true)
    @Override
    public StoreResponse getStore(GetStoreQuery query) {
        Store store = storeRepositoryPort.findById(query.storeId())
                .orElseThrow(() -> new BusinessException(NOT_FOUND_STORE));

        if (isNotOwner(store, query.userId())) {
            throw new BusinessException(NOT_AUTHORITY);
        }

        return StoreResponse.from(store);
    }

    @Transactional(readOnly = true)
    @Override
    public List<StoreResponse> getStores(GetStoresQuery query) {
        List<Store> stores = storeRepositoryPort.findAllByUserId(query.userId());
        return stores.stream().map(StoreResponse::from).toList();
    }

    @Transactional
    @Override
    public StoreResponse createStore(CreateStoreCommand command) {
        Store store = Store.builder()
                .userId(command.userId())
                .name(command.name())
                .address(command.address())
                .phoneNumber(command.phoneNumber())
                .businessNumber(command.businessNumber())
                .latitude(command.latitude())
                .longitude(command.longitude())
                .industry(command.industry())
                .build();

        Store saved = storeRepositoryPort.save(store);

        return StoreResponse.from(saved);
    }

    @Transactional
    @Override
    public StoreResponse updateStore(UpdateStoreCommand command) {
        Store store = storeRepositoryPort.findById(command.id())
                .orElseThrow(() -> new BusinessException(NOT_FOUND_STORE));

        if (isNotOwner(store, command.userId())) {
            throw new BusinessException(NOT_AUTHORITY);
        }

        store.update(
                command.name(),
                command.address(),
                command.phoneNumber(),
                command.latitude(),
                command.longitude(),
                command.industry()
        );

        Store updated = storeRepositoryPort.save(store);
        return StoreResponse.from(updated);
    }

    @Transactional
    @Override
    public void deleteStore(DeleteStoreCommand command) {
        Store store = storeRepositoryPort.findById(command.id())
                .orElseThrow(() -> new BusinessException(NOT_FOUND_STORE));

        if (isNotOwner(store, command.userId())) {
            throw new BusinessException(NOT_AUTHORITY);
        }

        storeRepositoryPort.delete(store);
    }

    private boolean isNotOwner(Store store, Long userId) {
        return !store.getUserId().equals(userId);
    }
}