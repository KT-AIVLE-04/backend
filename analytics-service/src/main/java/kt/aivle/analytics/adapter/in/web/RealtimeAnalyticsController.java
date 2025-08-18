package kt.aivle.analytics.adapter.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimeAccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimePostMetricsResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.application.service.YouTubeApiQuotaManager;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/analytics/realtime")
@RequiredArgsConstructor
public class RealtimeAnalyticsController {
    
    private final AnalyticsQueryUseCase analyticsQueryUseCase;
    private final YouTubeApiQuotaManager quotaManager;
    
    // GET /api/analytics/realtime/posts/{postId}/metrics
    @GetMapping("/posts/{postId}/metrics")
    public ResponseEntity<ApiResponse<List<RealtimePostMetricsResponse>>> getRealtimePostMetrics(
            @PathVariable String postId,
            @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (postId == null || !postId.matches("\\d+")) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<RealtimePostMetricsResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        PostMetricsQueryRequest request = PostMetricsQueryRequest.forCurrentDate(postId);
        if (!request.isValidIds()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<RealtimePostMetricsResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        List<RealtimePostMetricsResponse> response = analyticsQueryUseCase.getRealtimePostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/realtime/accounts/{accountId}/metrics
    @GetMapping("/accounts/{accountId}/metrics")
    public ResponseEntity<ApiResponse<List<RealtimeAccountMetricsResponse>>> getRealtimeAccountMetrics(
            @PathVariable String accountId,
            @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (accountId == null || !accountId.matches("\\d+")) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<RealtimeAccountMetricsResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        AccountMetricsQueryRequest request = AccountMetricsQueryRequest.forCurrentDate(accountId);
        if (!request.isValidAccountId()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<RealtimeAccountMetricsResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        List<RealtimeAccountMetricsResponse> response = analyticsQueryUseCase.getRealtimeAccountMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/realtime/posts/{postId}/comments?page=0&size=20
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<PostCommentsQueryResponse>>> getRealtimePostComments(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (postId == null || !postId.matches("\\d+")) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<PostCommentsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        if (page < 0 || size <= 0 || size > 100) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<PostCommentsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        PostCommentsQueryRequest request = PostCommentsQueryRequest.forCurrentDate(postId, page, size);
        if (!request.isValidRequest()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<PostCommentsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        List<PostCommentsQueryResponse> response = analyticsQueryUseCase.getRealtimePostComments(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/realtime/quota/status
    @GetMapping("/quota/status")
    public ResponseEntity<ApiResponse<YouTubeApiQuotaManager.QuotaStatus>> getQuotaStatus() {
        YouTubeApiQuotaManager.QuotaStatus status = quotaManager.getQuotaStatus();
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, status));
    }
}
