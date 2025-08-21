package kt.aivle.analytics.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
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
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 매일 오전 11시 30분에 메트릭 수집 배치 작업을 실행합니다.
     */
    @Scheduled(cron = "0 30 23 * * ?", zone = "Asia/Seoul")
    public void runDailyMetricsCollectionJob() {
        LocalDateTime startTime = LocalDateTime.now();
        String formattedTime = startTime.format(TIME_FORMATTER);
        
        log.info("🚀 Daily metrics collection batch job started at {}", formattedTime);
        
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", formattedTime)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            
            jobLauncher.run(dailyMetricsCollectionJob, jobParameters);
            
            LocalDateTime endTime = LocalDateTime.now();
            long totalDuration = System.currentTimeMillis() - startTime.getNano() / 1_000_000;
            
            log.info("✅ Daily metrics collection completed in {:.1f}s", totalDuration / 1000.0);
            
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("❌ Batch job is already running: {}", e.getMessage());
        } catch (JobRestartException e) {
            log.error("❌ Batch job restart failed: {}", e.getMessage());
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("❌ Batch job instance already completed: {}", e.getMessage());
        } catch (JobParametersInvalidException e) {
            log.error("❌ Invalid job parameters: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Daily metrics collection failed: {}", e.getMessage());
        }
    }
    
    /**
     * 테스트용 - 1분마다 실행 (개발 환경에서만 사용)
     * 현재 활성화됨 - 테스트용
     */
    // @Scheduled(cron = "0 */2 * * * ?", zone = "Asia/Seoul")
    // // @org.springframework.context.annotation.Profile("dev")
    // public void runTestMetricsCollectionJob() {
    //     LocalDateTime startTime = LocalDateTime.now();
    //     String formattedTime = startTime.format(TIME_FORMATTER);
        
    //     log.info("🧪 Test metrics collection started at {}", formattedTime);
        
    //     try {
    //         JobParameters jobParameters = new JobParametersBuilder()
    //             .addString("executionTime", formattedTime)
    //             .addLong("timestamp", System.currentTimeMillis())
    //             .addString("testRun", "true")
    //             .toJobParameters();
            
    //         jobLauncher.run(dailyMetricsCollectionJob, jobParameters);
            
    //         LocalDateTime endTime = LocalDateTime.now();
    //         long totalDuration = System.currentTimeMillis() - startTime.getNano() / 1_000_000;
            
    //         log.info("✅ Test metrics collection completed in {:.1f}s", totalDuration / 1000.0);
            
    //     } catch (Exception e) {
    //         log.error("❌ Test metrics collection failed: {}", e.getMessage());
    //     }
    // }
}
