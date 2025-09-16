package kt.aivle.shortsmvc.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobStatus {
    private String jobId;
    private Status status;
    private Integer progress;
    private String resultKey;
    private String error;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        QUEUED, RUNNING, SUCCEEDED, FAILED
    }

    public JobStatus(String jobId) {
        this.jobId = jobId;
        this.status = Status.QUEUED;
        this.progress = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}