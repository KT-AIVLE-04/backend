package kt.aivle.store.adapter.in.web.mapper;

import kt.aivle.store.adapter.in.web.dto.*;
import kt.aivle.store.application.port.in.command.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StoreCommandMapper {
    CreateStoreCommand toCreateCommand(CreateStoreRequest req, Long userId);
    UpdateStoreCommand toUpdateCommand(UpdateStoreRequest req, Long id, Long userId);
    DeleteStoreCommand toDeleteCommand(Long id, Long userId);
}
