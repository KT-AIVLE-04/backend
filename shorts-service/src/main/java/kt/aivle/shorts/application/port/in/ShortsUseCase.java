package kt.aivle.shorts.application.port.in;

import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.command.CreateShortsCommand;
import kt.aivle.shorts.application.port.in.command.SaveShortsCommand;
import kt.aivle.shorts.application.port.in.dto.ScenarioDTO;
import kt.aivle.shorts.application.port.in.dto.ShortsDTO;
import reactor.core.publisher.Mono;

public interface ShortsUseCase {
    Mono<ScenarioDTO> createScenario(CreateScenarioCommand command);

    Mono<ShortsDTO> createShorts(CreateShortsCommand command);

    Mono<Void> saveShorts(SaveShortsCommand command);
}
