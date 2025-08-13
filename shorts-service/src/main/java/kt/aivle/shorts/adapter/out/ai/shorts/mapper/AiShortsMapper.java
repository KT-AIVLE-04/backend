package kt.aivle.shorts.adapter.out.ai.shorts.mapper;

import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiScenarioRequest;
import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiScenarioResponse;
import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiShortsRequest;
import kt.aivle.shorts.adapter.out.ai.shorts.dto.CreateAiShortsResponse;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioResponse;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateShortsRequest;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateShortsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AiShortsMapper {

    @Mapping(target = "storeName", source = "storeName")
    @Mapping(target = "businessType", source = "businessType")
    @Mapping(target = "brandConcept", source = "brandConcepts")
    @Mapping(target = "platform", source = "platform")
    @Mapping(target = "adType", source = "adType")
    @Mapping(target = "targetAudience", source = "target")
    @Mapping(target = "scenarioPrompt", source = "prompt")
    CreateAiScenarioRequest toAiCreateScenarioRequest(GenerateScenarioRequest request);

    GenerateScenarioResponse toGenerateScenarioResponse(CreateAiScenarioResponse response);

    CreateAiShortsRequest toAiCreateShortsRequest(GenerateShortsRequest request);

    GenerateShortsResponse toGenerateShortsResponse(CreateAiShortsResponse response);
}