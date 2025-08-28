package kt.aivle.shorts.application.port.out.job;

import kt.aivle.shorts.application.port.out.ai.shorts.model.JobState;

import java.util.Optional;

public interface JobStore {
    void init(String jobId);

    void start(String jobId);

    void success(String jobId, String key);

    void fail(String jobId, String message);

    Optional<JobState> find(String jobId);

    void put(JobState st);

    void complete(String jobId);
}
