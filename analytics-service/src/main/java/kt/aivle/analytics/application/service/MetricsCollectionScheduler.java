package kt.aivle.analytics.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 매일 12시에 모든 계정과 게시물의 메트릭을 수집합니다.
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void collectDailyMetrics() {
        LocalDateTime startTime = LocalDateTime.now();
        String formattedTime = startTime.format(TIME_FORMATTER);
        
        log.info("🚀 ===== DAILY METRICS COLLECTION STARTED =====");
        log.info("⏰ Execution Time: {}", formattedTime);
        log.info("📊 Target: All accounts, posts, and comments");
        log.info("🎯 Schedule: Daily at 12:00 PM");
        log.info("�� Environment: Production");
        
        try {
            // Step 1: 계정 메트릭 수집
            log.info("📈 Step 1/3: Collecting account metrics...");
            log.info("   🔍 Target: All YouTube accounts");
            log.info("   📋 Process: Channel statistics collection");
            
            long step1Start = System.currentTimeMillis();
            metricsCollectionUseCase.collectAccountMetrics();
            long step1Duration = System.currentTimeMillis() - step1Start;
            
            log.info("✅ Step 1/3: Account metrics collection completed");
            log.info("   ⏱️ Duration: {} seconds", step1Duration / 1000.0);
            
            // Step 2: 게시물 메트릭 수집
            log.info("📈 Step 2/3: Collecting post metrics...");
            log.info("   🔍 Target: All YouTube videos");
            log.info("   📋 Process: Video statistics collection");
            
            long step2Start = System.currentTimeMillis();
            metricsCollectionUseCase.collectPostMetrics();
            long step2Duration = System.currentTimeMillis() - step2Start;
            
            log.info("✅ Step 2/3: Post metrics collection completed");
            log.info("   ⏱️ Duration: {} seconds", step2Duration / 1000.0);
            
            // Step 3: 게시물 댓글 수집
            log.info("📈 Step 3/3: Collecting post comments...");
            log.info("   🔍 Target: All video comments");
            log.info("   📋 Process: Comment thread collection");
            
            long step3Start = System.currentTimeMillis();
            metricsCollectionUseCase.collectPostComments();
            long step3Duration = System.currentTimeMillis() - step3Start;
            
            log.info("✅ Step 3/3: Post comments collection completed");
            log.info("   ⏱️ Duration: {} seconds", step3Duration / 1000.0);
            
            // 완료 로그
            LocalDateTime endTime = LocalDateTime.now();
            long totalDuration = System.currentTimeMillis() - startTime.getNano() / 1_000_000;
            
            log.info("🎉 ===== DAILY METRICS COLLECTION COMPLETED =====");
            log.info("⏱️ Total Duration: {} seconds", totalDuration / 1000.0);
            log.info("📅 Started: {}", startTime.format(TIME_FORMATTER));
            log.info("📅 Finished: {}", endTime.format(TIME_FORMATTER));
            log.info("🚀 Status: SUCCESS");
            log.info("💾 Data: Saved to database");
            log.info("📊 Summary: All metrics collected successfully");
            
        } catch (AnalyticsException e) {
            log.error("❌ ===== DAILY METRICS COLLECTION FAILED =====");
            log.error("🚨 Analytics Error: {}", e.getMessage());
            log.error("🔍 Error Type: AnalyticsException");
            log.error("📋 Details: {}", e.getLocalizedMessage());
            log.error("⏰ Failed at: {}", LocalDateTime.now().format(TIME_FORMATTER));
            
            // 배치 작업 실패 기록
            batchJobMonitor.recordJobFailure("daily-metrics-collection", e.getMessage());
            
        } catch (Exception e) {
            log.error("❌ ===== DAILY METRICS COLLECTION FAILED =====");
            log.error("🚨 Unexpected Error: {}", e.getMessage());
            log.error("🔍 Error Type: {}", e.getClass().getSimpleName());
            log.error("📋 Details: {}", e.getLocalizedMessage());
            log.error("⏰ Failed at: {}", LocalDateTime.now().format(TIME_FORMATTER));
            
            // 배치 작업 실패 기록
            batchJobMonitor.recordJobFailure("daily-metrics-collection", "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * 테스트용 - 1분마다 실행 (개발 환경에서만 사용)
     * 현재 비활성화됨 - 필요시 주석 해제
     */
    // @Scheduled(cron = "0 */1 * * * ?", zone = "Asia/Seoul")
    // @org.springframework.context.annotation.Profile("dev")
    public void collectTestMetrics() {
        LocalDateTime startTime = LocalDateTime.now();
        String formattedTime = startTime.format(TIME_FORMATTER);
        
        log.info("🧪 ===== TEST METRICS COLLECTION STARTED =====");
        log.info("⏰ Execution Time: {}", formattedTime);
        log.info("📊 Target: All accounts, posts, and comments");
        log.info("🎯 Schedule: Every minute (DEV ONLY)");
        log.info("🔧 Environment: Development");
        log.info("⚠️  WARNING: This is for testing purposes only!");
        
        try {
            // Step 1: 계정 메트릭 수집
            log.info("📈 Step 1/3: Collecting account metrics for test...");
            log.info("   🔍 Target: All YouTube accounts");
            log.info("   📋 Process: Channel statistics collection");
            
            long step1Start = System.currentTimeMillis();
            metricsCollectionUseCase.collectAccountMetrics();
            long step1Duration = System.currentTimeMillis() - step1Start;
            
            log.info("✅ Step 1/3: Account metrics collection completed");
            log.info("   ⏱️ Duration: {} seconds", step1Duration / 1000.0);
            
            // Step 2: 게시물 메트릭 수집
            log.info("�� Step 2/3: Collecting post metrics for test...");
            log.info("   🔍 Target: All YouTube videos");
            log.info("   📋 Process: Video statistics collection");
            
            long step2Start = System.currentTimeMillis();
            metricsCollectionUseCase.collectPostMetrics();
            long step2Duration = System.currentTimeMillis() - step2Start;
            
            log.info("✅ Step 2/3: Post metrics collection completed");
            log.info("   ⏱️ Duration: {} seconds", step2Duration / 1000.0);
            
            // Step 3: 게시물 댓글 수집
            log.info("📈 Step 3/3: Collecting post comments for test...");
            log.info("   🔍 Target: All video comments");
            log.info("   📋 Process: Comment thread collection");
            
            long step3Start = System.currentTimeMillis();
            metricsCollectionUseCase.collectPostComments();
            long step3Duration = System.currentTimeMillis() - step3Start;
            
            log.info("✅ Step 3/3: Post comments collection completed");
            log.info("   ⏱️ Duration: {} seconds", step3Duration / 1000.0);
            
            // 완료 로그
            LocalDateTime endTime = LocalDateTime.now();
            long totalDuration = System.currentTimeMillis() - startTime.getNano() / 1_000_000;
            
            log.info("🎉 ===== TEST METRICS COLLECTION COMPLETED =====");
            log.info("⏱️ Total Duration: {} seconds", totalDuration / 1000.0);
            log.info("📅 Started: {}", startTime.format(TIME_FORMATTER));
            log.info("📅 Finished: {}", endTime.format(TIME_FORMATTER));
            log.info("🚀 Status: SUCCESS");
            log.info("💾 Data: Saved to database");
            log.info("📊 Summary: All test metrics collected successfully");
            log.info("⚠️  REMINDER: This was a test run!");
            
        } catch (AnalyticsException e) {
            log.error("❌ ===== TEST METRICS COLLECTION FAILED =====");
            log.error("🚨 Analytics Error: {}", e.getMessage());
            log.error("🔍 Error Type: AnalyticsException");
            log.error("📋 Details: {}", e.getLocalizedMessage());
            log.error("⏰ Failed at: {}", LocalDateTime.now().format(TIME_FORMATTER));
            log.error("⚠️  This was a test run - errors are expected during development");
            
            // 배치 작업 실패 기록
            batchJobMonitor.recordJobFailure("test-metrics-collection", e.getMessage());
            
        } catch (Exception e) {
            log.error("❌ ===== TEST METRICS COLLECTION FAILED =====");
            log.error("🚨 Unexpected Error: {}", e.getMessage());
            log.error("🔍 Error Type: {}", e.getClass().getSimpleName());
            log.error("📋 Details: {}", e.getLocalizedMessage());
            log.error("⏰ Failed at: {}", LocalDateTime.now().format(TIME_FORMATTER));
            log.error("⚠️  This was a test run - errors are expected during development");
            
            // 배치 작업 실패 기록
            batchJobMonitor.recordJobFailure("test-metrics-collection", "Unexpected error: " + e.getMessage());
        }
    }
}
