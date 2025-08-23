package kt.aivle.analytics.adapter.in.web;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kt.aivle.analytics.adapter.in.web.dto.response.BatchJobStatusResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.BatchOperationResponse;
import kt.aivle.analytics.application.port.in.MetricsCollectionUseCase;
import kt.aivle.analytics.application.service.BatchJobMonitor;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
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
    private ResponseEntity<ApiResponse<BatchOperationResponse>> executeBatchOperation(
        String operationName, 
        Runnable operation
    ) {
        try {
            operation.run();
            BatchOperationResponse response = BatchOperationResponse.builder()
                .operationName(operationName)
                .status("SUCCESS")
                .executedAt(LocalDateTime.now())
                .message(operationName + " completed successfully")
                .build();
            
            return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
        } catch (BusinessException e) {
            log.error("Business error during {}: {}", operationName, e.getMessage(), e);
            BatchOperationResponse response = BatchOperationResponse.builder()
                .operationName(operationName)
                .status("FAILED")
                .executedAt(LocalDateTime.now())
                .message(e.getMessage())
                .build();
            
            return ResponseEntity.badRequest()
                .body(ApiResponse.of(CommonResponseCode.BAD_REQUEST, response));
        } catch (Exception e) {
            log.error("System error during {}", operationName, e);
            BatchOperationResponse response = BatchOperationResponse.builder()
                .operationName(operationName)
                .status("ERROR")
                .executedAt(LocalDateTime.now())
                .message("System error occurred")
                .build();
            
            return ResponseEntity.internalServerError()
                .body(ApiResponse.of(CommonResponseCode.INTERNAL_SERVER_ERROR, response));
        }
    }
    
    // POST /api/analytics/batch/accounts/metrics
    @PostMapping("/accounts/metrics")
    public ResponseEntity<ApiResponse<BatchOperationResponse>> collectAllAccountMetrics() {
        log.info("Manual account metrics collection requested");
        
        return executeBatchOperation("account metrics collection", () -> metricsCollectionUseCase.collectAccountMetrics());
    }
    
    // POST /api/analytics/batch/accounts/{accountId}/metrics
    @PostMapping("/accounts/{accountId}/metrics")
    public ResponseEntity<ApiResponse<BatchOperationResponse>> collectAccountMetrics(@PathVariable Long accountId) {
        log.info("Manual account metrics collection requested for local accountId: {}", accountId);
        
        return executeBatchOperation("account metrics collection for local accountId: " + accountId, () -> metricsCollectionUseCase.collectAccountMetricsByAccountId(accountId));
    }
    
    // POST /api/analytics/batch/posts/metrics
    @PostMapping("/posts/metrics")
    public ResponseEntity<ApiResponse<BatchOperationResponse>> collectAllPostMetrics() {
        log.info("Manual post metrics collection requested");
        
        return executeBatchOperation("post metrics collection", () -> metricsCollectionUseCase.collectPostMetrics());
    }
    
    // POST /api/analytics/batch/posts/{postId}/metrics
    @PostMapping("/posts/{postId}/metrics")
    public ResponseEntity<ApiResponse<BatchOperationResponse>> collectPostMetrics(@PathVariable Long postId) {
        log.info("Manual post metrics collection requested for local postId: {}", postId);
        
        return executeBatchOperation("post metrics collection for local postId: " + postId, () -> metricsCollectionUseCase.collectPostMetricsByPostId(postId));
    }
    
    // POST /api/analytics/batch/posts/comments
    @PostMapping("/posts/comments")
    public ResponseEntity<ApiResponse<BatchOperationResponse>> collectAllPostComments() {
        log.info("Manual post comments collection requested");
        
        return executeBatchOperation("post comments collection", () -> metricsCollectionUseCase.collectPostComments());
    }
    
    // POST /api/analytics/batch/posts/{postId}/comments
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<BatchOperationResponse>> collectPostComments(@PathVariable Long postId) {
        log.info("Manual post comments collection requested for local postId: {}", postId);
        
        return executeBatchOperation("post comments collection for local postId: " + postId, () -> metricsCollectionUseCase.collectPostCommentsByPostId(postId));
    }
    
    // POST /api/analytics/batch/metrics
    @PostMapping("/metrics")
    public ResponseEntity<ApiResponse<BatchOperationResponse>> collectAllMetrics() {
        log.info("Manual all metrics collection requested");
        
        return executeBatchOperation("all metrics collection", () -> {
            metricsCollectionUseCase.collectAccountMetrics();
            metricsCollectionUseCase.collectPostMetrics();
            metricsCollectionUseCase.collectPostComments();
        });
    }
    
    // GET /api/analytics/batch/status
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, BatchJobStatusResponse>>> getAllBatchJobStatuses() {
        log.info("Batch job statuses requested");
        
        Map<String, BatchJobMonitor.BatchJobStatus> statuses = batchJobMonitor.getAllJobStatuses();
        Map<String, BatchJobStatusResponse> responseMap = statuses.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> convertToBatchJobStatusResponse(entry.getKey(), entry.getValue())
            ));
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, responseMap));
    }
    
    // GET /api/analytics/batch/status/{jobName}
    @GetMapping("/status/{jobName}")
    public ResponseEntity<ApiResponse<BatchJobStatusResponse>> getBatchJobStatus(@PathVariable String jobName) {
        log.info("Batch job status requested for jobName: {}", jobName);
        
        BatchJobMonitor.BatchJobStatus status = batchJobMonitor.getJobStatus(jobName);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        
        BatchJobStatusResponse response = convertToBatchJobStatusResponse(jobName, status);
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    private BatchJobStatusResponse convertToBatchJobStatusResponse(String jobName, BatchJobMonitor.BatchJobStatus status) {
        return BatchJobStatusResponse.builder()
            .jobName(jobName)
            .status(status.getStatus())
            .startTime(status.getStartTime())
            .endTime(status.getEndTime())
            .progress(status.getProcessed())
            .totalItems(status.getTotal())
            .errorMessage(status.getError())
            .build();
    }
}
