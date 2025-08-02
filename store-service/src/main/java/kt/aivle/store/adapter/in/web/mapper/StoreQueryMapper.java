package kt.aivle.store.adapter.in.web.mapper;

import kt.aivle.store.application.port.in.query.GetStoreQuery;
import kt.aivle.store.application.port.in.query.GetStoresQuery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StoreQueryMapper {
    GetStoreQuery toGetStoreQuery(Long storeId, Long userId);

    GetStoresQuery toGetStoresQuery(Long userId);
}
