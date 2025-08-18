package kt.aivle.analytics.adapter.in.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostMetricsQueryRequest;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics/history")
@RequiredArgsConstructor
public class HistoricalAnalyticsController {
    
    private final AnalyticsQueryUseCase analyticsQueryUseCase;
    
    // GET /api/analytics/history/posts/{postId}/metrics?date=2024-01-15
    @GetMapping("/posts/{postId}/metrics")
    public ResponseEntity<ApiResponse<List<PostMetricsQueryResponse>>> getHistoricalPostMetrics(
            @PathVariable String postId,
            @RequestParam LocalDate date,
            @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (postId == null || !postId.matches("\\d+")) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<PostMetricsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        if (date == null || date.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<PostMetricsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        PostMetricsQueryRequest request = PostMetricsQueryRequest.forDate(date, null, postId);
        if (!request.isValidIds() || !request.isValidDate()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<PostMetricsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        List<PostMetricsQueryResponse> response = analyticsQueryUseCase.getPostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/history/accounts/{accountId}/metrics?date=2024-01-15
    @GetMapping("/accounts/{accountId}/metrics")
    public ResponseEntity<ApiResponse<List<AccountMetricsQueryResponse>>> getHistoricalAccountMetrics(
            @PathVariable String accountId,
            @RequestParam LocalDate date,
            @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (accountId == null || !accountId.matches("\\d+")) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<AccountMetricsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        if (date == null || date.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<AccountMetricsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        AccountMetricsQueryRequest request = AccountMetricsQueryRequest.forDate(date, accountId);
        if (!request.isValidAccountId() || !request.isValidDate()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<AccountMetricsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        List<AccountMetricsQueryResponse> response = analyticsQueryUseCase.getAccountMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/history/posts/{postId}/comments?date=2024-01-15&page=0&size=20
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<PostCommentsQueryResponse>>> getHistoricalPostComments(
            @PathVariable String postId,
            @RequestParam LocalDate date,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (postId == null || !postId.matches("\\d+")) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<PostCommentsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        if (date == null || date.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<PostCommentsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        if (page < 0 || size <= 0 || size > 100) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<PostCommentsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        PostCommentsQueryRequest request = PostCommentsQueryRequest.forDate(date, postId, page, size);
        if (!request.isValidRequest() || !request.isValidDate()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.<List<PostCommentsQueryResponse>>of(CommonResponseCode.BAD_REQUEST, null));
        }
        
        List<PostCommentsQueryResponse> response = analyticsQueryUseCase.getPostComments(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/history/accounts/{accountId}/posts/metrics?date=2024-01-15
    @GetMapping("/accounts/{accountId}/posts/metrics")
    public ResponseEntity<ApiResponse<List<PostMetricsQueryResponse>>> getHistoricalAccountPostsMetrics(
            @PathVariable String accountId,
            @RequestParam LocalDate date,
            @RequestHeader("X-USER-ID") String userId) {
        
        PostMetricsQueryRequest request = PostMetricsQueryRequest.forAccount(date, accountId);
        List<PostMetricsQueryResponse> response = analyticsQueryUseCase.getPostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // GET /api/analytics/history/users/{userId}/metrics?date=2024-01-15
    @GetMapping("/users/{userId}/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistoricalUserMetrics(
            @PathVariable String userId,
            @RequestParam LocalDate date,
            @RequestHeader("X-USER-ID") String currentUserId) {
        
        // 사용자 본인의 데이터만 조회 가능하도록 검증
        if (!userId.equals(currentUserId)) {
            return ResponseEntity.status(403).build();
        }
        
        AccountMetricsQueryRequest accountRequest = AccountMetricsQueryRequest.forAllAccounts(date);
        PostMetricsQueryRequest postRequest = PostMetricsQueryRequest.forAllPosts(date);
        
        List<AccountMetricsQueryResponse> accountMetrics = analyticsQueryUseCase.getAccountMetrics(userId, accountRequest);
        List<PostMetricsQueryResponse> postMetrics = analyticsQueryUseCase.getPostMetrics(userId, postRequest);
        
        Map<String, Object> response = Map.of(
            "accountMetrics", accountMetrics,
            "postMetrics", postMetrics
        );
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
}
