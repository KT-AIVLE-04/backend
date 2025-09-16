package kt.aivle.shortsmvc.storage;

import kt.aivle.shortsmvc.model.JobStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class JobStore {
    private final Map<String, JobStatus> jobs = new ConcurrentHashMap<>();

    public void init(String jobId) {
        jobs.put(jobId, new JobStatus(jobId));
    }

    public void start(String jobId) {
        JobStatus job = jobs.get(jobId);
        if (job != null) {
            job.setStatus(JobStatus.Status.RUNNING);
            job.setProgress(10);
            job.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void updateProgress(String jobId, int progress) {
        JobStatus job = jobs.get(jobId);
        if (job != null) {
            job.setProgress(progress);
            job.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void success(String jobId, String resultKey) {
        JobStatus job = jobs.get(jobId);
        if (job != null) {
            job.setStatus(JobStatus.Status.SUCCEEDED);
            job.setProgress(100);
            job.setResultKey(resultKey);
            job.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void fail(String jobId, String error) {
        JobStatus job = jobs.get(jobId);
        if (job != null) {
            job.setStatus(JobStatus.Status.FAILED);
            job.setError(error);
            job.setUpdatedAt(LocalDateTime.now());
        }
    }

    public JobStatus find(String jobId) {
        return jobs.get(jobId);
    }
}