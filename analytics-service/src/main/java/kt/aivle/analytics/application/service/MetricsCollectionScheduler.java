package kt.aivle.analytics.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kt.aivle.analytics.application.port.in.MetricsCollectionUseCase;
import kt.aivle.analytics.exception.AnalyticsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsCollectionScheduler {
    
    private final MetricsCollectionUseCase metricsCollectionUseCase;
    private final BatchJobMonitor batchJobMonitor;
    
    /**
     * 매일 12시에 모든 계정과 게시물의 메트릭을 수집합니다.
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void collectDailyMetrics() {
        log.info("Starting daily metrics collection at 12:00");
        
        try {
            // 계정 메트릭 수집
            log.info("Collecting account metrics...");
            metricsCollectionUseCase.collectAccountMetrics();
            
            // 게시물 메트릭 수집
            log.info("Collecting post metrics...");
            metricsCollectionUseCase.collectPostMetrics();
            
            // 게시물 댓글 수집
            log.info("Collecting post comments...");
            metricsCollectionUseCase.collectPostComments();
            
            log.info("Daily metrics collection completed successfully");
            
        } catch (AnalyticsException e) {
            log.error("Analytics error during daily metrics collection: {}", e.getMessage(), e);
            // 배치 작업 실패 기록
            batchJobMonitor.recordJobFailure("daily-metrics-collection", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during daily metrics collection", e);
            // 배치 작업 실패 기록
            batchJobMonitor.recordJobFailure("daily-metrics-collection", "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * 테스트용 - 1분마다 실행 (개발 환경에서만 사용)
     */
    @Scheduled(cron = "0 */1 * * * ?", zone = "Asia/Seoul")
    @org.springframework.context.annotation.Profile("dev")
    public void collectTestMetrics() {
        log.info("Starting test metrics collection (every minute)");
        
        try {
            // 계정 메트릭 수집
            log.info("Collecting account metrics for test...");
            metricsCollectionUseCase.collectAccountMetrics();
            
            // 게시물 메트릭 수집
            log.info("Collecting post metrics for test...");
            metricsCollectionUseCase.collectPostMetrics();
            
            // 게시물 댓글 수집
            log.info("Collecting post comments for test...");
            metricsCollectionUseCase.collectPostComments();
            
            log.info("Test metrics collection completed successfully");
            
        } catch (AnalyticsException e) {
            log.error("Analytics error during test metrics collection: {}", e.getMessage(), e);
            // 배치 작업 실패 기록
            batchJobMonitor.recordJobFailure("test-metrics-collection", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during test metrics collection", e);
            // 배치 작업 실패 기록
            batchJobMonitor.recordJobFailure("test-metrics-collection", "Unexpected error: " + e.getMessage());
        }
    }
}
