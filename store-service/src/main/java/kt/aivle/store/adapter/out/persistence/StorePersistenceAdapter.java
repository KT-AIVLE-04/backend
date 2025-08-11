package kt.aivle.store.adapter.out.persistence;

import kt.aivle.store.application.port.out.StoreRepositoryPort;
import kt.aivle.store.domain.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StorePersistenceAdapter implements StoreRepositoryPort {

    private final JpaStoreRepository storeRepository;

    @Override
    public Store save(Store store) {
        return storeRepository.save(store);
    }

    @Override
    public Optional<Store> findById(Long id) {
        return storeRepository.findById(id);
    }

    @Override
    public List<Store> findAllByUserId(Long userId) {
        return storeRepository.findAllByUserId(userId);
    }

    @Override
    public void delete(Store store) {
        storeRepository.delete(store);
    }
}
