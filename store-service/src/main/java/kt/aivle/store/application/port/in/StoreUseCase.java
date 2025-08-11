package kt.aivle.store.application.port.in;

import kt.aivle.store.adapter.in.web.dto.StoreResponse;
import kt.aivle.store.application.port.in.command.CreateStoreCommand;
import kt.aivle.store.application.port.in.command.DeleteStoreCommand;
import kt.aivle.store.application.port.in.command.UpdateStoreCommand;
import kt.aivle.store.application.port.in.query.GetStoreQuery;
import kt.aivle.store.application.port.in.query.GetStoresQuery;

import java.util.List;

public interface StoreUseCase {
    StoreResponse getStore(GetStoreQuery query);

    List<StoreResponse> getStores(GetStoresQuery query);

    StoreResponse createStore(CreateStoreCommand command);

    StoreResponse updateStore(UpdateStoreCommand command);

    void deleteStore(DeleteStoreCommand command);
}
