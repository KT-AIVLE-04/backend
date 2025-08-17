package kt.aivle.analytics.application.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class BatchJobMonitor {
    
    private final Map<String, BatchJobStatus> jobStatuses = new ConcurrentHashMap<>();
    
    public void recordJobStart(String jobName) {
        BatchJobStatus status = new BatchJobStatus(jobName, LocalDateTime.now());
        jobStatuses.put(jobName, status);
        log.info("Batch job started: {}", jobName);
    }
    
    public void recordJobProgress(String jobName, int processed, int total) {
        BatchJobStatus status = jobStatuses.get(jobName);
        if (status != null) {
            status.updateProgress(processed, total);
            log.info("Batch job progress - {}: {}/{} ({}%)", 
                jobName, processed, total, (processed * 100 / total));
        }
    }
    
    public void recordJobSuccess(String jobName) {
        BatchJobStatus status = jobStatuses.get(jobName);
        if (status != null) {
            status.completeSuccess();
            log.info("Batch job completed successfully: {}", jobName);
        }
    }
    
    public void recordJobFailure(String jobName, String error) {
        BatchJobStatus status = jobStatuses.get(jobName);
        if (status != null) {
            status.completeFailure(error);
            log.error("Batch job failed: {} - {}", jobName, error);
        }
    }
    
    public BatchJobStatus getJobStatus(String jobName) {
        return jobStatuses.get(jobName);
    }
    
    public Map<String, BatchJobStatus> getAllJobStatuses() {
        return new ConcurrentHashMap<>(jobStatuses);
    }
    
    @Getter
    public static class BatchJobStatus {
        private final String jobName;
        private final LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status; // RUNNING, SUCCESS, FAILED
        private int processed;
        private int total;
        private String error;
        
        public BatchJobStatus(String jobName, LocalDateTime startTime) {
            this.jobName = jobName;
            this.startTime = startTime;
            this.status = "RUNNING";
            this.processed = 0;
            this.total = 0;
        }
        
        public void updateProgress(int processed, int total) {
            this.processed = processed;
            this.total = total;
        }
        
        public void completeSuccess() {
            this.endTime = LocalDateTime.now();
            this.status = "SUCCESS";
        }
        
        public void completeFailure(String error) {
            this.endTime = LocalDateTime.now();
            this.status = "FAILED";
            this.error = error;
        }
        
        public long getDurationSeconds() {
            LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
            return java.time.Duration.between(startTime, end).getSeconds();
        }
        
        public int getProgressPercentage() {
            return total > 0 ? (processed * 100 / total) : 0;
        }
    }
}
