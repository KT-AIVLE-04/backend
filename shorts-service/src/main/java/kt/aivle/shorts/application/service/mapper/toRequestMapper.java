package kt.aivle.shorts.application.service.mapper;

import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.command.CreateShortsCommand;
import kt.aivle.shorts.application.port.in.command.SaveShortsCommand;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateShortsRequest;
import kt.aivle.shorts.application.port.out.event.contents.CreateContentRequest;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoRequest;
import kt.aivle.shorts.application.port.out.event.store.StoreInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface toRequestMapper {
    @Mapping(target = "storeName", source = "storeInfo.storeName")
    @Mapping(target = "businessType", source = "storeInfo.businessType")
    @Mapping(target = "brandConcepts", source = "command.brandConcepts")
    @Mapping(target = "platform", source = "command.platform")
    @Mapping(target = "adType", source = "command.adType")
    @Mapping(target = "target", source = "command.target")
    @Mapping(target = "prompt", source = "command.prompt")
    GenerateScenarioRequest toGenerateScenarioRequest(
            CreateScenarioCommand command,
            StoreInfoResponse storeInfo
    );

    StoreInfoRequest toStoreInfoRequest(Long userId, Long storeId);

    GenerateShortsRequest toGenerateShortsRequest(CreateShortsCommand command, List<String> imageUrls);

    @Mapping(target = "url", source = "command.videoUrl")
    CreateContentRequest toCreateContentRequest(SaveShortsCommand command);
}
