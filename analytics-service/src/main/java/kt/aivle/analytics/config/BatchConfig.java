package kt.aivle.analytics.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import kt.aivle.analytics.application.port.in.MetricsCollectionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    
    private final MetricsCollectionUseCase metricsCollectionUseCase;
    
    @Bean
    public Job dailyMetricsCollectionJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("dailyMetricsCollectionJob", jobRepository)
            .start(collectAccountMetricsStep(jobRepository, transactionManager))
            .next(collectPostMetricsStep(jobRepository, transactionManager))
            .next(collectPostCommentsStep(jobRepository, transactionManager))
            .build();
    }
    
    @Bean
    public Step collectAccountMetricsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("collectAccountMetricsStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("🚀 Starting account metrics collection step");
                
                try {
                    metricsCollectionUseCase.collectAccountMetrics();
                    log.info("✅ Account metrics collection completed");
                    return RepeatStatus.FINISHED;
                } catch (Exception e) {
                    log.error("❌ Account metrics collection failed: {}", e.getMessage(), e);
                    throw e;
                }
            }, transactionManager)
            .build();
    }
    
    @Bean
    public Step collectPostMetricsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("collectPostMetricsStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("🚀 Starting post metrics collection step");
                
                try {
                    metricsCollectionUseCase.collectPostMetrics();
                    log.info("✅ Post metrics collection completed");
                    return RepeatStatus.FINISHED;
                } catch (Exception e) {
                    log.error("❌ Post metrics collection failed: {}", e.getMessage(), e);
                    throw e;
                }
            }, transactionManager)
            .build();
    }
    
    @Bean
    public Step collectPostCommentsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("collectPostCommentsStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("🚀 Starting post comments collection step");
                
                try {
                    metricsCollectionUseCase.collectPostComments();
                    log.info("✅ Post comments collection completed");
                    return RepeatStatus.FINISHED;
                } catch (Exception e) {
                    log.error("❌ Post comments collection failed: {}", e.getMessage(), e);
                    throw e;
                }
            }, transactionManager)
            .build();
    }
}
