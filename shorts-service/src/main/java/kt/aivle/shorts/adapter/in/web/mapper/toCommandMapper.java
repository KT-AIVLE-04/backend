package kt.aivle.shorts.adapter.in.web.mapper;

import kt.aivle.shorts.adapter.in.web.dto.request.CreateScenarioRequest;
import kt.aivle.shorts.adapter.in.web.dto.request.CreateShortsRequest;
import kt.aivle.shorts.adapter.in.web.dto.request.SaveShortsRequest;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.command.CreateShortsCommand;
import kt.aivle.shorts.application.port.in.command.SaveShortsCommand;
import org.mapstruct.Mapper;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

@Mapper(componentModel = "spring")
public interface toCommandMapper {
    CreateScenarioCommand toCreateScenarioCommand(Long userId, Long storeId, CreateScenarioRequest req);

    CreateShortsCommand toCreateShortsCommand(CreateShortsRequest request, Flux<FilePart> images);

    SaveShortsCommand toSaveShortsCommand(Long userId, Long storeId, SaveShortsRequest request);
}
