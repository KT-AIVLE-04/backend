package kt.aivle.shorts.application.port.in;

import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.command.CreateShortsCommand;
import kt.aivle.shorts.application.port.in.command.SaveShortsCommand;
import kt.aivle.shorts.application.port.in.dto.ScenarioDTO;
import reactor.core.publisher.Mono;

public interface ShortsUseCase {
    Mono<ScenarioDTO> createScenario(CreateScenarioCommand command);

    //    Mono<ShortsDTO> createShorts(CreateShortsCommand command);
    void startCreateShorts(CreateShortsCommand command);

    Mono<Void> saveShorts(SaveShortsCommand command);
}
