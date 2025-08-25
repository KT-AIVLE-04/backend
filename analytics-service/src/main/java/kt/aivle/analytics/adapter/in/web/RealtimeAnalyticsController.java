package kt.aivle.analytics.adapter.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kt.aivle.analytics.adapter.in.web.dto.response.AccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostCommentsResponse;
import kt.aivle.analytics.adapter.in.web.dto.response.PostMetricsResponse;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/analytics/realtime")
@RequiredArgsConstructor
@Tag(name = "Realtime Analytics", description = "실시간 분석 API")
public class RealtimeAnalyticsController {
    
    private final AnalyticsQueryUseCase analyticsQueryUseCase;
    
    @Operation(summary = "실시간 게시물 메트릭 조회", description = "특정 게시물의 실시간 메트릭을 조회합니다.")
    @GetMapping("/posts/metrics")
    public ResponseEntity<ApiResponse<PostMetricsResponse>> getRealtimePostMetrics(
            @RequestParam("accountId") Long accountId,
            @RequestParam(value = "postId", required = false) String postId,
            @RequestHeader("X-USER-ID") String userId) {
        
        PostMetricsResponse response = analyticsQueryUseCase.getRealtimePostMetrics(userId, accountId, postId);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "실시간 계정 메트릭 조회", description = "특정 계정의 실시간 메트릭을 조회합니다.")
    @GetMapping("/accounts/metrics")
    public ResponseEntity<ApiResponse<AccountMetricsResponse>> getRealtimeAccountMetrics(
            @RequestParam("accountId") Long accountId,
            @RequestHeader("X-USER-ID") String userId) {
        
        AccountMetricsResponse response = analyticsQueryUseCase.getRealtimeAccountMetrics(userId, accountId);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "실시간 게시물 댓글 조회", description = "특정 게시물의 실시간 댓글을 페이지네이션으로 조회합니다.")
    @GetMapping("/posts/comments")
    public ResponseEntity<ApiResponse<List<PostCommentsResponse>>> getRealtimePostComments(
            @RequestParam("accountId") Long accountId,
            @RequestParam(value = "postId", required = false) String postId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestHeader("X-USER-ID") String userId) {
        
        List<PostCommentsResponse> response = analyticsQueryUseCase.getRealtimePostComments(userId, accountId, postId, page, size);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
}


