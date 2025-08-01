package kt.aivle.store.adapter.out.persistence;


import kt.aivle.store.domain.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaStoreRepository extends JpaRepository<Store, Long> {
    List<Store> findAllByUserId(Long userId);
}
