package kt.aivle.analytics.adapter.in.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.analytics.application.service.YouTubeApiQuotaManager;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final AnalyticsQueryUseCase analyticsQueryUseCase;
    private final YouTubeApiQuotaManager quotaManager;
    
    // GET /api/analytics/posts/{postId}/metrics?dateRange=current|1week
    @GetMapping("/posts/{postId}/metrics")
    public ResponseEntity<ApiResponse<List<PostMetricsQueryResponse>>> getPostMetrics(
            @PathVariable String postId,
            @RequestParam(defaultValue = "1week") String dateRange,
            @RequestHeader("X-USER-ID") String userId) {
        
        PostMetricsQueryRequest request = new PostMetricsQueryRequest(dateRange, null, postId);
        List<PostMetricsQueryResponse> response = analyticsQueryUseCase.getPostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/accounts/{accountId}/metrics?dateRange=current|1month
    @GetMapping("/accounts/{accountId}/metrics")
    public ResponseEntity<ApiResponse<List<AccountMetricsQueryResponse>>> getAccountMetrics(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "1month") String dateRange,
            @RequestHeader("X-USER-ID") String userId) {
        
        AccountMetricsQueryRequest request = new AccountMetricsQueryRequest(dateRange, accountId);
        List<AccountMetricsQueryResponse> response = analyticsQueryUseCase.getAccountMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/posts/{postId}/comments? |1week&page=0&size=20
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<PostCommentsQueryResponse>>> getPostComments(
            @PathVariable String postId,
            @RequestParam(defaultValue = "1week") String dateRange,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestHeader("X-USER-ID") String userId) {
        
        PostCommentsQueryRequest request = new PostCommentsQueryRequest(dateRange, postId, page, size);
        List<PostCommentsQueryResponse> response = analyticsQueryUseCase.getPostComments(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/accounts/{accountId}/posts/metrics?dateRange=current|1week
    @GetMapping("/accounts/{accountId}/posts/metrics")
    public ResponseEntity<ApiResponse<List<PostMetricsQueryResponse>>> getAccountPostsMetrics(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "1week") String dateRange,
            @RequestHeader("X-USER-ID") String userId) {
        
        PostMetricsQueryRequest request = new PostMetricsQueryRequest(dateRange, accountId, null);
        List<PostMetricsQueryResponse> response = analyticsQueryUseCase.getPostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/users/{userId}/metrics?dateRange=current|1month
    @GetMapping("/users/{userId}/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserMetrics(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1month") String dateRange,
            @RequestHeader("X-USER-ID") String currentUserId) {
        
        // 사용자 본인의 데이터만 조회 가능하도록 검증
        if (!userId.equals(currentUserId)) {
            return ResponseEntity.status(403).build();
        }
        
        AccountMetricsQueryRequest accountRequest = new AccountMetricsQueryRequest(dateRange, null);
        PostMetricsQueryRequest postRequest = new PostMetricsQueryRequest(dateRange, null, null);
        
        List<AccountMetricsQueryResponse> accountMetrics = analyticsQueryUseCase.getAccountMetrics(userId, accountRequest);
        List<PostMetricsQueryResponse> postMetrics = analyticsQueryUseCase.getPostMetrics(userId, postRequest);
        
        Map<String, Object> response = Map.of(
            "accountMetrics", accountMetrics,
            "postMetrics", postMetrics
        );
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/quota/status
    @GetMapping("/quota/status")
    public ResponseEntity<ApiResponse<YouTubeApiQuotaManager.QuotaStatus>> getQuotaStatus() {
        YouTubeApiQuotaManager.QuotaStatus status = quotaManager.getQuotaStatus();
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, status));
    }
}
