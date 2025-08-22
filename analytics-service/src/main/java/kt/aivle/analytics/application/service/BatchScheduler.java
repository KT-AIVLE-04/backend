package kt.aivle.analytics.application.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {
    
    private final JobLauncher jobLauncher;
    private final Job dailyMetricsCollectionJob;
    
    /**
     * 매일 오전 1시에 메트릭 수집 배치 작업을 실행합니다.
     */
    @Scheduled(cron = "0 22 1 * * ?", zone = "Asia/Seoul")
    public void runDailyMetricsCollectionJob() {
        log.info("🚀 Daily metrics collection batch job started");
        
        try {
            // YAML 설정으로 중복 실행 방지 및 재시작 설정이 처리됨
            jobLauncher.run(dailyMetricsCollectionJob, new JobParameters());
            log.info("✅ Daily metrics collection completed");
            
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("❌ Batch job is already running: {}", e.getMessage());
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("❌ Batch job instance already completed: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Daily metrics collection failed: {}", e.getMessage());
        }
    }
    
    /**
     * 테스트용 - 5분마다 실행 (개발 환경에서만 사용)
     */
    // @Scheduled(cron = "0 */5 * * * ?", zone = "Asia/Seoul")
    // @org.springframework.context.annotation.Profile("dev")
    public void runTestMetricsCollectionJob() {
        log.info("🧪 Test metrics collection started");
        
        try {
            jobLauncher.run(dailyMetricsCollectionJob, new JobParameters());
            log.info("✅ Test metrics collection completed");
            
        } catch (Exception e) {
            log.error("❌ Test metrics collection failed: {}", e.getMessage());
        }
    }
}
