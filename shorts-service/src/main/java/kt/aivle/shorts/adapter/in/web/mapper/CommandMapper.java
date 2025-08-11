package kt.aivle.shorts.adapter.in.web.mapper;

import kt.aivle.shorts.adapter.in.web.dto.request.CreateScenarioRequest;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import org.mapstruct.Mapper;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

@Mapper(componentModel = "spring")
public interface CommandMapper {
    CreateScenarioCommand toCreateScenarioCommand(Long userId, Long storeId, CreateScenarioRequest req, Flux<FilePart> images);

//    CreateSceneCommand toCreateSceneCommand(String sessionId, String title, String content);
}
