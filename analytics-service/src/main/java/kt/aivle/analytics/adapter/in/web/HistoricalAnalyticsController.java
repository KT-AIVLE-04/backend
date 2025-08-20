package kt.aivle.analytics.adapter.in.web;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.validator.AnalyticsRequestValidator;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.exception.AnalyticsValidationException;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/analytics/history")
@RequiredArgsConstructor
@Tag(name = "Historical Analytics", description = "히스토리 분석 API")
public class HistoricalAnalyticsController {
    
    private final AnalyticsQueryUseCase analyticsQueryUseCase;
    private final AnalyticsRequestValidator validator;
    
    @Operation(summary = "히스토리 게시물 메트릭 조회", description = "특정 날짜의 게시물 메트릭 히스토리를 조회합니다.")
    @GetMapping("/posts/{postId}/metrics")
    public ResponseEntity<ApiResponse<List<PostMetricsQueryResponse>>> getHistoricalPostMetrics(
            @Parameter(description = "게시물 ID") @PathVariable String postId,
            @Parameter(description = "조회할 날짜") @RequestParam Date date,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidPostId(postId)) {
            throw AnalyticsValidationException.invalidPostId(postId);
        }
        
        if (!validator.isValidUserId(userId)) {
            throw AnalyticsValidationException.invalidUserId(userId);
        }
        
        if (date == null || date.after(new Date())) {
            throw AnalyticsValidationException.invalidDate("Date cannot be null or in the future");
        }
        
        PostMetricsQueryRequest request = PostMetricsQueryRequest.forDate(date, null, postId);
        if (!request.isValidIds() || !request.isValidDate()) {
            throw AnalyticsValidationException.invalidPostId(postId);
        }
        
        List<PostMetricsQueryResponse> response = analyticsQueryUseCase.getPostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "히스토리 계정 메트릭 조회", description = "특정 날짜의 계정 메트릭 히스토리를 조회합니다.")
    @GetMapping("/accounts/{accountId}/metrics")
    public ResponseEntity<ApiResponse<List<AccountMetricsQueryResponse>>> getHistoricalAccountMetrics(
            @Parameter(description = "계정 ID") @PathVariable String accountId,
            @Parameter(description = "조회할 날짜") @RequestParam Date date,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidAccountId(accountId)) {
            throw AnalyticsValidationException.invalidAccountId(accountId);
        }
        
        if (!validator.isValidUserId(userId)) {
            throw AnalyticsValidationException.invalidUserId(userId);
        }
        
        if (date == null || date.after(new Date())) {
            throw AnalyticsValidationException.invalidDate("Date cannot be null or in the future");
        }
        
        AccountMetricsQueryRequest request = AccountMetricsQueryRequest.forDate(date, accountId);
        if (!request.isValidAccountId() || !request.isValidDate()) {
            throw AnalyticsValidationException.invalidAccountId(accountId);
        }
        
        List<AccountMetricsQueryResponse> response = analyticsQueryUseCase.getAccountMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "히스토리 게시물 댓글 조회", description = "특정 날짜의 게시물 댓글 히스토리를 페이지네이션으로 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<PostCommentsQueryResponse>>> getHistoricalPostComments(
            @Parameter(description = "게시물 ID") @PathVariable String postId,
            @Parameter(description = "조회할 날짜") @RequestParam Date date,
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
        
        if (date == null || date.after(new Date())) {
            throw AnalyticsValidationException.invalidDate("Date cannot be null or in the future");
        }
        
        if (!validator.isValidPagination(page, size)) {
            throw AnalyticsValidationException.invalidPagination(page, size);
        }
        
        PostCommentsQueryRequest request = PostCommentsQueryRequest.forDate(date, postId, page, size);
        if (!request.isValidRequest()) {
            throw AnalyticsValidationException.invalidPostId(postId);
        }
        
        List<PostCommentsQueryResponse> response = analyticsQueryUseCase.getPostComments(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "히스토리 계정 게시물 메트릭 조회", description = "특정 날짜의 계정의 모든 게시물 메트릭 히스토리를 조회합니다.")
    @GetMapping("/accounts/{accountId}/posts/metrics")
    public ResponseEntity<ApiResponse<List<PostMetricsQueryResponse>>> getHistoricalAccountPostsMetrics(
            @Parameter(description = "계정 ID") @PathVariable String accountId,
            @Parameter(description = "조회할 날짜") @RequestParam Date date,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidAccountId(accountId)) {
            throw AnalyticsValidationException.invalidAccountId(accountId);
        }
        
        if (!validator.isValidUserId(userId)) {
            throw AnalyticsValidationException.invalidUserId(userId);
        }
        
        if (date == null || date.after(new Date())) {
            throw AnalyticsValidationException.invalidDate("Date cannot be null or in the future");
        }
        
        PostMetricsQueryRequest request = PostMetricsQueryRequest.forAccount(date, accountId);
        List<PostMetricsQueryResponse> response = analyticsQueryUseCase.getPostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "히스토리 사용자 메트릭 조회", description = "특정 날짜의 사용자의 모든 계정 및 게시물 메트릭 히스토리를 조회합니다.")
    @GetMapping("/users/{userId}/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistoricalUserMetrics(
            @Parameter(description = "사용자 ID") @PathVariable String userId,
            @Parameter(description = "조회할 날짜") @RequestParam Date date,
            @Parameter(description = "현재 사용자 ID") @RequestHeader("X-USER-ID") String currentUserId) {
        
        // 입력 검증
        if (!validator.isValidUserId(userId)) {
            throw AnalyticsValidationException.invalidUserId(userId);
        }
        
        if (!validator.isValidUserId(currentUserId)) {
            throw AnalyticsValidationException.invalidUserId(currentUserId);
        }
        
        if (date == null || date.after(new Date())) {
            throw AnalyticsValidationException.invalidDate("Date cannot be null or in the future");
        }
        
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
