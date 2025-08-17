package kt.aivle.analytics.adapter.in.web;

import kt.aivle.analytics.application.port.in.MetricsCollectionUseCase;
import kt.aivle.analytics.application.service.BatchJobMonitor;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analytics/batch")
@RequiredArgsConstructor
public class BatchController {
    
    private final MetricsCollectionUseCase metricsCollectionUseCase;
    private final BatchJobMonitor batchJobMonitor;
    
    /**
     * 모든 계정의 메트릭을 수동으로 수집합니다.
     */
    @PostMapping("/collect-account-metrics")
    public ResponseEntity<ApiResponse<String>> collectAccountMetrics() {
        log.info("Manual account metrics collection requested");
        
        try {
            metricsCollectionUseCase.collectAccountMetrics();
            return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, "Account metrics collection completed"));
        } catch (Exception e) {
            log.error("Failed to collect account metrics", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.of(CommonResponseCode.INTERNAL_SERVER_ERROR, "Failed to collect account metrics"));
        }
    }
    
    /**
     * 모든 게시물의 메트릭을 수동으로 수집합니다.
     */
    @PostMapping("/collect-post-metrics")
    public ResponseEntity<ApiResponse<String>> collectPostMetrics() {
        log.info("Manual post metrics collection requested");
        
        try {
            metricsCollectionUseCase.collectPostMetrics();
            return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, "Post metrics collection completed"));
        } catch (Exception e) {
            log.error("Failed to collect post metrics", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.of(CommonResponseCode.INTERNAL_SERVER_ERROR, "Failed to collect post metrics"));
        }
    }
    
    /**
     * 특정 계정의 메트릭을 수동으로 수집합니다.
     */
    @PostMapping("/collect-account-metrics/{accountId}")
    public ResponseEntity<ApiResponse<String>> collectAccountMetricsByAccountId(@PathVariable Long accountId) {
        log.info("Manual account metrics collection requested for accountId: {}", accountId);
        
        try {
            metricsCollectionUseCase.collectAccountMetricsByAccountId(accountId);
            return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, "Account metrics collection completed for accountId: " + accountId));
        } catch (Exception e) {
            log.error("Failed to collect account metrics for accountId: {}", accountId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.of(CommonResponseCode.INTERNAL_SERVER_ERROR, "Failed to collect account metrics for accountId: " + accountId));
        }
    }
    
    /**
     * 특정 게시물의 메트릭을 수동으로 수집합니다.
     */
    @PostMapping("/collect-post-metrics/{postId}")
    public ResponseEntity<ApiResponse<String>> collectPostMetricsByPostId(@PathVariable Long postId) {
        log.info("Manual post metrics collection requested for postId: {}", postId);
        
        try {
            metricsCollectionUseCase.collectPostMetricsByPostId(postId);
            return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, "Post metrics collection completed for postId: " + postId));
        } catch (Exception e) {
            log.error("Failed to collect post metrics for postId: {}", postId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.of(CommonResponseCode.INTERNAL_SERVER_ERROR, "Failed to collect post metrics for postId: " + postId));
        }
    }
    
    /**
     * 모든 게시물의 댓글을 수동으로 수집합니다.
     */
    @PostMapping("/collect-post-comments")
    public ResponseEntity<ApiResponse<String>> collectPostComments() {
        log.info("Manual post comments collection requested");
        
        try {
            metricsCollectionUseCase.collectPostComments();
            return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, "Post comments collection completed"));
        } catch (Exception e) {
            log.error("Failed to collect post comments", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.of(CommonResponseCode.INTERNAL_SERVER_ERROR, "Failed to collect post comments"));
        }
    }
    
    /**
     * 특정 게시물의 댓글을 수동으로 수집합니다.
     */
    @PostMapping("/collect-post-comments/{postId}")
    public ResponseEntity<ApiResponse<String>> collectPostCommentsByPostId(@PathVariable Long postId) {
        log.info("Manual post comments collection requested for postId: {}", postId);
        
        try {
            metricsCollectionUseCase.collectPostCommentsByPostId(postId);
            return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, "Post comments collection completed for postId: " + postId));
        } catch (Exception e) {
            log.error("Failed to collect post comments for postId: {}", postId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.of(CommonResponseCode.INTERNAL_SERVER_ERROR, "Failed to collect post comments for postId: " + postId));
        }
    }
    
    /**
     * 모든 메트릭을 수동으로 수집합니다.
     */
    @PostMapping("/collect-all-metrics")
    public ResponseEntity<ApiResponse<String>> collectAllMetrics() {
        log.info("Manual all metrics collection requested");
        
        try {
            metricsCollectionUseCase.collectAccountMetrics();
            metricsCollectionUseCase.collectPostMetrics();
            metricsCollectionUseCase.collectPostComments();
            return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, "All metrics collection completed"));
        } catch (Exception e) {
            log.error("Failed to collect all metrics", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.of(CommonResponseCode.INTERNAL_SERVER_ERROR, "Failed to collect all metrics"));
        }
    }
    
    /**
     * 모든 배치 작업 상태를 조회합니다.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, BatchJobMonitor.BatchJobStatus>>> getAllBatchJobStatuses() {
        log.info("Batch job statuses requested");
        
        Map<String, BatchJobMonitor.BatchJobStatus> statuses = batchJobMonitor.getAllJobStatuses();
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, statuses));
    }
    
    /**
     * 특정 배치 작업 상태를 조회합니다.
     */
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
