package kt.aivle.shorts.adapter.out.job;

import kt.aivle.shorts.application.port.out.ai.shorts.model.JobState;
import kt.aivle.shorts.application.port.out.ai.shorts.model.JobStatus;
import kt.aivle.shorts.application.port.out.job.JobStore;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryJobStore implements JobStore {

    private final ConcurrentMap<String, JobState> jobs = new ConcurrentHashMap<>();

    @Override
    public void init(String jobId) {
        jobs.put(jobId, new JobState(jobId, JobStatus.QUEUED, 0, null, null,
                System.currentTimeMillis(), null, null));
    }

    @Override
    public void start(String jobId) {
        find(jobId).ifPresent(s -> s.setStatus(JobStatus.RUNNING));
    }

    @Override
    public void success(String jobId, String key) {
        find(jobId).ifPresent(s -> {
            s.setStatus(JobStatus.SUCCEEDED);
            s.setProgress(100);
            s.setResultKey(key);
            dispose(s);
        });
    }

    @Override
    public void fail(String jobId, String message) {
        find(jobId).ifPresent(s -> {
            s.setStatus(JobStatus.FAILED);
            s.setError(message);
            dispose(s);
        });
    }

    @Override
    public Optional<JobState> find(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    @Override
    public void put(JobState st) {
        jobs.put(st.getJobId(), st);
    }

    @Override
    public void complete(String jobId) {
        find(jobId).ifPresent(this::dispose);
    }

    private void dispose(JobState s) {
        if (s.getProgressTask() != null) s.getProgressTask().dispose();
        if (s.getWatchTask() != null) s.getWatchTask().dispose();
    }
}
