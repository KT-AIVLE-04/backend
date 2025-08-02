package kt.aivle.store.application.port.out;

import kt.aivle.store.domain.model.Store;

import java.util.List;
import java.util.Optional;

public interface StoreRepositoryPort {
    Store save(Store store);

    Optional<Store> findById(Long id);

    List<Store> findAllByUserId(Long userId);

    void delete(Store store);
}
