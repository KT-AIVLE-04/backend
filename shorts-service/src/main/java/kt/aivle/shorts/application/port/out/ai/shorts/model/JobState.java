package kt.aivle.shorts.application.port.out.ai.shorts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.Disposable;

@Data
@AllArgsConstructor
public class JobState {
    private String jobId;
    private JobStatus status;
    private int progress;
    private String resultKey;
    private String error;
    private long startedAtMillis;
    private Disposable progressTask;
    private Disposable watchTask;
}
