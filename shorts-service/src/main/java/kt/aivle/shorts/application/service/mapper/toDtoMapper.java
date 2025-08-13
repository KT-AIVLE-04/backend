package kt.aivle.shorts.application.service.mapper;

import kt.aivle.shorts.application.port.in.dto.ScenarioDTO;
import kt.aivle.shorts.application.port.in.dto.ShortsDTO;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateScenarioResponse;
import kt.aivle.shorts.application.port.out.ai.shorts.dto.GenerateShortsResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface toDtoMapper {
    ScenarioDTO toScenarioDTO(GenerateScenarioResponse response);

    ShortsDTO toShortsDTO(GenerateShortsResponse response);
}
