package kt.aivle.shorts.adapter.in.web.mapper;

import kt.aivle.shorts.adapter.in.web.dto.response.ScenarioResponse;
import kt.aivle.shorts.adapter.in.web.dto.response.ShortsResponse;
import kt.aivle.shorts.application.port.in.dto.ScenarioDTO;
import kt.aivle.shorts.application.port.in.dto.ShortsDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface toResponseMapper {
    ScenarioResponse toScenarioResponse(ScenarioDTO result);

    ShortsResponse toShortsResponse(ShortsDTO result);
}
