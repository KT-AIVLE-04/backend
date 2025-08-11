package kt.aivle.shorts.application.service.mapper;

import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.dto.ScenarioDTO;
import kt.aivle.shorts.application.port.out.ai.shorts.GenerateScenarioRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.GenerateScenarioResponse;
import kt.aivle.shorts.application.port.out.event.contents.CreateContentRequest;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoRequest;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    StoreInfoRequest toStoreInfoResult(CreateScenarioCommand command, String correlationId);

    ScenarioDTO toScenarioDTO(GenerateScenarioResponse response);

    @Mapping(target = "storeName", source = "storeInfo.storeName")
    @Mapping(target = "businessType", source = "storeInfo.businessType")
    @Mapping(target = "brandConcepts", source = "command.brandConcepts")
    @Mapping(target = "imageUrls", source = "imageList")
    @Mapping(target = "platform", source = "command.platform")
    @Mapping(target = "adType", source = "command.adType")
    @Mapping(target = "target", source = "command.target")
    @Mapping(target = "prompt", source = "command.prompt")
    GenerateScenarioRequest toGenerateScenarioRequest(
            CreateScenarioCommand command,
            StoreInfoResponse storeInfo,
            List<String> imageList
    );

    @Mapping(target = "images", source = "items")
    CreateContentRequest toCreateContentRequestMessage(Long storeId, List<CreateContentRequest.ImageItem> items);
}
