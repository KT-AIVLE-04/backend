package kt.aivle.analytics.application.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BatchJobMonitor {
    
    private final Map<String, BatchJobStatus> jobStatuses = new ConcurrentHashMap<>();
    
    public void recordJobStart(String jobName) {
        BatchJobStatus status = new BatchJobStatus();
        status.setStatus("RUNNING");
        status.setStartTime(LocalDateTime.now());
        status.setProcessed(0);
        status.setTotal(0);
        status.setError(null);
        
        jobStatuses.put(jobName, status);
        log.info("Batch job started: {}", jobName);
    }
    
    public void recordJobProgress(String jobName, int processed, int total) {
        BatchJobStatus status = jobStatuses.get(jobName);
        if (status != null) {
            status.setProcessed(processed);
            status.setTotal(total);
            log.debug("Batch job progress - {}: {}/{}", jobName, processed, total);
        }
    }
    
    public void recordJobSuccess(String jobName) {
        BatchJobStatus status = jobStatuses.get(jobName);
        if (status != null) {
            status.setStatus("COMPLETED");
            status.setEndTime(LocalDateTime.now());
            log.info("Batch job completed successfully: {}", jobName);
        }
    }
    
    public void recordJobFailure(String jobName, String errorMessage) {
        BatchJobStatus status = jobStatuses.get(jobName);
        if (status != null) {
            status.setStatus("FAILED");
            status.setEndTime(LocalDateTime.now());
            status.setError(errorMessage);
            log.error("Batch job failed: {} - {}", jobName, errorMessage);
        }
    }
    
    public BatchJobStatus getJobStatus(String jobName) {
        return jobStatuses.get(jobName);
    }
    
    public Map<String, BatchJobStatus> getAllJobStatuses() {
        return new ConcurrentHashMap<>(jobStatuses);
    }
    
    public static class BatchJobStatus {
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int processed;
        private int total;
        private String error;
        
        // Getters and Setters
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public LocalDateTime getStartTime() {
            return startTime;
        }
        
        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }
        
        public LocalDateTime getEndTime() {
            return endTime;
        }
        
        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }
        
        public int getProcessed() {
            return processed;
        }
        
        public void setProcessed(int processed) {
            this.processed = processed;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public String getError() {
            return error;
        }
        
        public void setError(String error) {
            this.error = error;
        }
    }
}
