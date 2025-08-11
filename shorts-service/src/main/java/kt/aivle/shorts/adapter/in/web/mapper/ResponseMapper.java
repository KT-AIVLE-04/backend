package kt.aivle.shorts.adapter.in.web.mapper;

import kt.aivle.shorts.adapter.in.web.dto.response.ScenarioResponse;
import kt.aivle.shorts.application.port.in.dto.ScenarioDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResponseMapper {
    ScenarioResponse toScenarioResponse(ScenarioDTO result);
}
