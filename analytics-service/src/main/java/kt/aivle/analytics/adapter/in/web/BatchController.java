package kt.aivle.analytics.adapter.in.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kt.aivle.analytics.application.port.in.MetricsCollectionUseCase;
import kt.aivle.analytics.application.service.BatchJobMonitor;
import kt.aivle.analytics.exception.AnalyticsException;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/analytics/batch")
@RequiredArgsConstructor
public class BatchController {
    
    private final MetricsCollectionUseCase metricsCollectionUseCase;
    private final BatchJobMonitor batchJobMonitor;
    
    // 공통 예외 처리 메서드
    private ResponseEntity<ApiResponse<String>> executeBatchOperation(
        String operationName, 
        Runnable operation
    ) {
        try {
            operation.run();
            return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, operationName + " completed"));
        } catch (AnalyticsException e) {
            log.error("Analytics error during {}: {}", operationName, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.of(CommonResponseCode.BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            log.error("System error during {}", operationName, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.of(CommonResponseCode.INTERNAL_SERVER_ERROR, "System error occurred"));
        }
    }
    
    // POST /api/analytics/batch/accounts/metrics
    @PostMapping("/accounts/metrics")
    public ResponseEntity<ApiResponse<String>> collectAllAccountMetrics() {
        log.info("Manual account metrics collection requested");
        
        return executeBatchOperation("account metrics collection", () -> metricsCollectionUseCase.collectAccountMetrics());
    }
    
    // POST /api/analytics/batch/accounts/{accountId}/metrics
    @PostMapping("/accounts/{accountId}/metrics")
    public ResponseEntity<ApiResponse<String>> collectAccountMetrics(@PathVariable Long accountId) {
        log.info("Manual account metrics collection requested for accountId: {}", accountId);
        
        return executeBatchOperation("account metrics collection for accountId: " + accountId, () -> metricsCollectionUseCase.collectAccountMetricsByAccountId(accountId));
    }
    
    // POST /api/analytics/batch/posts/metrics
    @PostMapping("/posts/metrics")
    public ResponseEntity<ApiResponse<String>> collectAllPostMetrics() {
        log.info("Manual post metrics collection requested");
        
        return executeBatchOperation("post metrics collection", () -> metricsCollectionUseCase.collectPostMetrics());
    }
    
    // POST /api/analytics/batch/posts/{postId}/metrics
    @PostMapping("/posts/{postId}/metrics")
    public ResponseEntity<ApiResponse<String>> collectPostMetrics(@PathVariable Long postId) {
        log.info("Manual post metrics collection requested for postId: {}", postId);
        
        return executeBatchOperation("post metrics collection for postId: " + postId, () -> metricsCollectionUseCase.collectPostMetricsByPostId(postId));
    }
    
    // POST /api/analytics/batch/posts/comments
    @PostMapping("/posts/comments")
    public ResponseEntity<ApiResponse<String>> collectAllPostComments() {
        log.info("Manual post comments collection requested");
        
        return executeBatchOperation("post comments collection", () -> metricsCollectionUseCase.collectPostComments());
    }
    
    // POST /api/analytics/batch/posts/{postId}/comments
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<String>> collectPostComments(@PathVariable Long postId) {
        log.info("Manual post comments collection requested for postId: {}", postId);
        
        return executeBatchOperation("post comments collection for postId: " + postId, () -> metricsCollectionUseCase.collectPostCommentsByPostId(postId));
    }
    
    // POST /api/analytics/batch/metrics
    @PostMapping("/metrics")
    public ResponseEntity<ApiResponse<String>> collectAllMetrics() {
        log.info("Manual all metrics collection requested");
        
        return executeBatchOperation("all metrics collection", () -> {
            metricsCollectionUseCase.collectAccountMetrics();
            metricsCollectionUseCase.collectPostMetrics();
            metricsCollectionUseCase.collectPostComments();
        });
    }
    
    // GET /api/analytics/batch/status
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, BatchJobMonitor.BatchJobStatus>>> getAllBatchJobStatuses() {
        log.info("Batch job statuses requested");
        
        Map<String, BatchJobMonitor.BatchJobStatus> statuses = batchJobMonitor.getAllJobStatuses();
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, statuses));
    }
    
    // GET /api/analytics/batch/status/{jobName}
    @GetMapping("/status/{jobName}")
    public ResponseEntity<ApiResponse<BatchJobMonitor.BatchJobStatus>> getBatchJobStatus(@PathVariable String jobName) {
        log.info("Batch job status requested for jobName: {}", jobName);
        
        BatchJobMonitor.BatchJobStatus status = batchJobMonitor.getJobStatus(jobName);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, status));
    }
}
