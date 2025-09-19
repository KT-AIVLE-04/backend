package kt.aivle.shorts.adapter.out.event.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortsCompensationService {

    public void compensateShortsCreation(String sagaId, Long userId, Long storeId, String key) {
        log.info("Starting shorts compensation for sagaId: {}, userId: {}, storeId: {}, key: {}",
                sagaId, userId, storeId, key);

        try {
            // Shorts 관련 보상 트랜잭션 로직
            // 예: 임시 파일 삭제, 작업 상태 롤백 등

            // 1. S3에서 임시 파일 삭제 (key 기반)
            cleanupTempFiles(key);

            // 2. 작업 상태 롤백
            resetJobStatus(sagaId);

            log.info("Successfully completed shorts compensation for sagaId: {}", sagaId);

        } catch (Exception e) {
            log.error("Failed to compensate shorts creation for sagaId: {}, error: {}",
                    sagaId, e.getMessage(), e);
            throw e;
        }
    }

    private void cleanupTempFiles(String key) {
        try {
            // S3Storage나 MediaStoragePort를 통해 임시 파일 삭제
            log.info("Cleaning up temp files for key: {}", key);
            // mediaStoragePort.deleteTempFile(key);
        } catch (Exception e) {
            log.error("Failed to cleanup temp files for key: {}, error: {}", key, e.getMessage());
        }
    }

    private void resetJobStatus(String sagaId) {
        try {
            // JobStore에서 작업 상태를 실패로 설정하거나 삭제
            log.info("Resetting job status for sagaId: {}", sagaId);
            // jobStore.fail(sagaId, "Compensated due to saga failure");
        } catch (Exception e) {
            log.error("Failed to reset job status for sagaId: {}, error: {}", sagaId, e.getMessage());
        }
    }
}