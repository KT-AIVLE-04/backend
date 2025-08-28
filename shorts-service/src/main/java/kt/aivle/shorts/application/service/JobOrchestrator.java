package kt.aivle.shorts.application.service;

import kt.aivle.shorts.application.port.out.ai.shorts.model.JobState;
import kt.aivle.shorts.application.port.out.ai.shorts.model.JobStatus;
import kt.aivle.shorts.application.port.out.job.JobStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class JobOrchestrator {

    private final JobStore jobStore;

    private final long avgSeconds = 480;
    private final long tickSeconds = 5;

    public JobState start(String jobId) {
        JobState st = jobStore.find(jobId).orElseThrow();
        st.setStatus(JobStatus.RUNNING);
        st.setStartedAtMillis(System.currentTimeMillis());
        st.setProgress(0);
        jobStore.put(st);

        Disposable progress = Flux.interval(Duration.ofSeconds(tickSeconds))
                .takeWhile(tick -> {
                    JobState s = jobStore.find(jobId).orElse(null);
                    return s != null && s.getStatus() == JobStatus.RUNNING && s.getProgress() < 95;
                })
                .subscribe(tick -> jobStore.find(jobId).ifPresent(s -> {
                    int next = timelineProgress(s.getStartedAtMillis(), avgSeconds, s.getProgress());
                    int capped = Math.min(95, Math.max(s.getProgress(), Math.min(next, s.getProgress() + 2)));
                    s.setProgress(capped);
                }));

        st.setProgressTask(progress);
        return st;
    }

    public void complete(String jobId, String resultKey) {
        jobStore.success(jobId, resultKey);
    }

    public void fail(String jobId, String message) {
        jobStore.fail(jobId, message);
    }

    /**
     * 경과 시간 기반 진행률 계산(최대 95%)
     * - 0~8%: 준비/업로드(빠른 편, 전체의 5%)
     * - 8~92%: 본 처리(가장 길게, 전체의 90%)
     * - 92~95%: 패키징/업로드(마무리, 전체의 5%)
     * - 95~100%는 실제 완료 이벤트로만 진입
     */
    private int timelineProgress(long startedAtMillis, long targetSeconds, int current) {
        long elapsedSec = Math.max(0, (System.currentTimeMillis() - startedAtMillis) / 1000);
        if (targetSeconds <= 0) targetSeconds = 1200;

        double p1 = 0.05;
        double p2 = 0.90;
        double p3 = 0.05;

        long t1 = Math.round(targetSeconds * p1);
        long t2 = Math.round(targetSeconds * p2);
        long t3 = Math.max(1, targetSeconds - t1 - t2);

        double prog;
        if (elapsedSec <= t1) {
            double x = (double) elapsedSec / t1;
            prog = 8.0 * easeOutQuad(x); // 0~8%
        } else if (elapsedSec <= t1 + t2) {
            double x = (double) (elapsedSec - t1) / t2;
            prog = 8.0 + 84.0 * x; // 8~92%
        } else if (elapsedSec <= t1 + t2 + t3) {
            double x = (double) (elapsedSec - t1 - t2) / t3;
            prog = 92.0 + 3.0 * easeInQuad(x);
        } else {
            prog = 95.0;
        }

        int next = (int) Math.floor(prog);
        return Math.max(current, Math.min(next, 95));
    }

    private double easeOutQuad(double x) {
        return 1 - (1 - x) * (1 - x);
    }

    private double easeInQuad(double x) {
        return x * x;
    }
}