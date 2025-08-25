package kt.aivle.analytics.application.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
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
     * 매일 메트릭 수집 배치 작업을 실행합니다.
     * 스케줄은 application.yml에서 설정
     */
    @Scheduled(cron = "${app.batch.schedule.daily-metrics}", zone = "${app.batch.timezone}")
    public void runDailyMetricsCollectionJob() {
        log.info("🚀 Daily metrics collection batch job started");
        
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            jobLauncher.run(dailyMetricsCollectionJob, jobParameters);
            log.info("✅ Daily metrics collection completed");
            
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("❌ Batch job is already running: {}", e.getMessage());
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("❌ Batch job instance already completed: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Daily metrics collection failed: {}", e.getMessage());
        }
    }
}
