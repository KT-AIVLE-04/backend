package kt.aivle.shorts.adapter.in.web.mapper;

import kt.aivle.shorts.adapter.in.web.dto.ScenarioRequest;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import org.mapstruct.Mapper;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

@Mapper(componentModel = "spring")
public interface ScenarioCommandMapper {
    CreateScenarioCommand toCreateCommand(Long userId, Long storeId, ScenarioRequest req, Flux<FilePart> images);
}
