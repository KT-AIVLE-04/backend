package kt.aivle.analytics.adapter.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final AnalyticsQueryUseCase analyticsQueryUseCase;
    
    @GetMapping("/post-metrics")
    public ResponseEntity<ApiResponse<List<PostMetricsQueryResponse>>> getPostMetrics(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PostMetricsQueryRequest request) {
        
        List<PostMetricsQueryResponse> response = analyticsQueryUseCase.getPostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @GetMapping("/account-metrics")
    public ResponseEntity<ApiResponse<List<AccountMetricsQueryResponse>>> getAccountMetrics(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody AccountMetricsQueryRequest request) {
        
        List<AccountMetricsQueryResponse> response = analyticsQueryUseCase.getAccountMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @GetMapping("/post-comments")
    public ResponseEntity<ApiResponse<List<PostCommentsQueryResponse>>> getPostComments(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PostCommentsQueryRequest request) {
        
        List<PostCommentsQueryResponse> response = analyticsQueryUseCase.getPostComments(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
}
