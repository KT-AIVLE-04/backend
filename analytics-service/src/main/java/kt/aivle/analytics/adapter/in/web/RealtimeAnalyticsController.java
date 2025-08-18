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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimeAccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimePostMetricsResponse;
import kt.aivle.analytics.adapter.in.web.validator.AnalyticsRequestValidator;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.application.service.YouTubeApiQuotaManager;
import kt.aivle.analytics.exception.AnalyticsValidationException;
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
    private final YouTubeApiQuotaManager quotaManager;
    private final AnalyticsRequestValidator validator;
    
    @Operation(summary = "실시간 게시물 메트릭 조회", description = "특정 게시물의 실시간 메트릭을 조회합니다.")
    @GetMapping("/posts/{postId}/metrics")
    public ResponseEntity<ApiResponse<List<RealtimePostMetricsResponse>>> getRealtimePostMetrics(
            @Parameter(description = "게시물 ID") @PathVariable String postId,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidPostId(postId)) {
            throw AnalyticsValidationException.invalidPostId(postId);
        }
        
        if (!validator.isValidUserId(userId)) {
            throw AnalyticsValidationException.invalidUserId(userId);
        }
        
        PostMetricsQueryRequest request = PostMetricsQueryRequest.forCurrentDate(postId);
        if (!request.isValidIds()) {
            throw AnalyticsValidationException.invalidPostId(postId);
        }
        
        List<RealtimePostMetricsResponse> response = analyticsQueryUseCase.getRealtimePostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "실시간 계정 메트릭 조회", description = "특정 계정의 실시간 메트릭을 조회합니다.")
    @GetMapping("/accounts/{accountId}/metrics")
    public ResponseEntity<ApiResponse<List<RealtimeAccountMetricsResponse>>> getRealtimeAccountMetrics(
            @Parameter(description = "계정 ID") @PathVariable String accountId,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidAccountId(accountId)) {
            throw AnalyticsValidationException.invalidAccountId(accountId);
        }
        
        if (!validator.isValidUserId(userId)) {
            throw AnalyticsValidationException.invalidUserId(userId);
        }
        
        AccountMetricsQueryRequest request = AccountMetricsQueryRequest.forCurrentDate(accountId);
        if (!request.isValidAccountId()) {
            throw AnalyticsValidationException.invalidAccountId(accountId);
        }
        
        List<RealtimeAccountMetricsResponse> response = analyticsQueryUseCase.getRealtimeAccountMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "실시간 게시물 댓글 조회", description = "특정 게시물의 실시간 댓글을 페이지네이션으로 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<PostCommentsQueryResponse>>> getRealtimePostComments(
            @Parameter(description = "게시물 ID") @PathVariable String postId,
            @Parameter(description = "페이지 번호 (기본값: 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidPostId(postId)) {
            throw AnalyticsValidationException.invalidPostId(postId);
        }
        
        if (!validator.isValidUserId(userId)) {
            throw AnalyticsValidationException.invalidUserId(userId);
        }
        
        if (!validator.isValidPagination(page, size)) {
            throw AnalyticsValidationException.invalidPagination(page, size);
        }
        
        PostCommentsQueryRequest request = PostCommentsQueryRequest.forCurrentDate(postId, page, size);
        if (!request.isValidRequest()) {
            throw AnalyticsValidationException.invalidPostId(postId);
        }
        
        List<PostCommentsQueryResponse> response = analyticsQueryUseCase.getRealtimePostComments(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "YouTube API 할당량 상태 조회", description = "YouTube API 할당량 사용 현황을 조회합니다.")
    @GetMapping("/quota/status")
    public ResponseEntity<ApiResponse<YouTubeApiQuotaManager.QuotaStatus>> getQuotaStatus() {
        YouTubeApiQuotaManager.QuotaStatus status = quotaManager.getQuotaStatus();
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, status));
    }
}
