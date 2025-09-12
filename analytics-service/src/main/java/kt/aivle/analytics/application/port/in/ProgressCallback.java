package kt.aivle.analytics.application.port.in;

@FunctionalInterface
public interface ProgressCallback {
    void onProgress(int percentage, String message);
}
