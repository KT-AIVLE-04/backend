package kt.aivle.shorts.adapter.out.event.store;

import kt.aivle.shorts.adapter.out.event.store.dto.StoreInfoRequestMessage;
import kt.aivle.shorts.adapter.out.event.store.dto.StoreInfoResponseMessage;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoRequest;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StoreServiceMapper {

    StoreInfoRequestMessage toStoreInfoRequestMessage(StoreInfoRequest req);

    @Mapping(target = "storeName", source = "name")
    @Mapping(target = "businessType", source = "industry")
    StoreInfoResponse toStoreInfoResponse(StoreInfoResponseMessage msg);
}
