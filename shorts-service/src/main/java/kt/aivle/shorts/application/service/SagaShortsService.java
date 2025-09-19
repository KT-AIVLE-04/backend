package kt.aivle.shorts.application.service;

import kt.aivle.shorts.adapter.out.event.saga.SagaOrchestrator;
import kt.aivle.shorts.application.port.in.command.SaveShortsCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaShortsService {

    private final SagaOrchestrator sagaOrchestrator;

    public void saveShortsWithSaga(SaveShortsCommand command) {
        String sagaId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();

        log.info("Starting Saga for saveShorts: sagaId={}, userId={}, storeId={}, key={}",
                sagaId, command.userId(), command.storeId(), command.key());

        sagaOrchestrator.startSaga(sagaId, requestId, command.userId(), command.storeId(), command.key());
    }
}