package kt.aivle.shorts.adapter.out.event.store.mapper;

import kt.aivle.shorts.adapter.out.event.store.dto.StoreInfoRequestMessage;
import kt.aivle.shorts.adapter.out.event.store.dto.StoreInfoResponseMessage;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoRequest;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StoreServiceMapper {

    @Mapping(target = "requestId", source = "correlationId")
    StoreInfoRequestMessage toMessage(StoreInfoRequest req);

    @Mapping(target = "storeName", source = "name")
    @Mapping(target = "businessType", source = "industry")
    StoreInfoResponse toResponse(StoreInfoResponseMessage msg);
}
