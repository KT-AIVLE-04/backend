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
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostCommentsQueryRequest;
import kt.aivle.analytics.application.port.in.dto.PostMetricsQueryRequest;
import kt.aivle.analytics.domain.model.SnsType;
import kt.aivle.analytics.exception.AnalyticsErrorCode;
import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.exception.BusinessException;
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
    public ResponseEntity<ApiResponse<List<PostMetricsResponse>>> getRealtimePostMetrics(
            @RequestParam("snsType") String snsType,
            @RequestParam(value = "postId", required = false) String postId,
            @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (snsType == null || snsType.trim().isEmpty()) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        SnsType snsTypeEnum;
        try {
            snsTypeEnum = SnsType.valueOf(snsType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        PostMetricsQueryRequest queryRequest;
        if (postId != null && !postId.trim().isEmpty()) {
            queryRequest = PostMetricsQueryRequest.forCurrentDate(postId);
        } else {
            queryRequest = PostMetricsQueryRequest.forLatestPostBySnsType(userId, snsTypeEnum);
        }
        
        List<PostMetricsResponse> response = analyticsQueryUseCase.getRealtimePostMetrics(userId, queryRequest);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "실시간 계정 메트릭 조회", description = "특정 계정의 실시간 메트릭을 조회합니다.")
    @GetMapping("/accounts/metrics")
    public ResponseEntity<ApiResponse<AccountMetricsResponse>> getRealtimeAccountMetrics(
            @RequestParam("snsType") String snsType,
            @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (snsType == null || snsType.trim().isEmpty()) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        SnsType snsTypeEnum;
        try {
            snsTypeEnum = SnsType.valueOf(snsType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        AccountMetricsQueryRequest queryRequest = AccountMetricsQueryRequest.forCurrentDateAndSnsType(userId, snsTypeEnum);
        
        List<AccountMetricsResponse> responseList = analyticsQueryUseCase.getRealtimeAccountMetrics(userId, queryRequest);
        
        // SNS 타입이 필수이므로 첫 번째 결과만 반환 (계정은 SNS 타입별로 하나씩만 존재)
        AccountMetricsResponse response = responseList.isEmpty() ? null : responseList.get(0);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "실시간 게시물 댓글 조회", description = "특정 게시물의 실시간 댓글을 페이지네이션으로 조회합니다.")
    @GetMapping("/posts/comments")
    public ResponseEntity<ApiResponse<List<PostCommentsResponse>>> getRealtimePostComments(
            @RequestParam("snsType") String snsType,
            @RequestParam(value = "postId", required = false) String postId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (snsType == null || snsType.trim().isEmpty()) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        SnsType snsTypeEnum;
        try {
            snsTypeEnum = SnsType.valueOf(snsType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        PostCommentsQueryRequest queryRequest;
        if (postId != null && !postId.trim().isEmpty()) {
            queryRequest = PostCommentsQueryRequest.forCurrentDate(postId, page, size);
        } else {
            queryRequest = PostCommentsQueryRequest.forLatestPostBySnsType(userId, snsTypeEnum, page, size);
        }
        
        List<PostCommentsResponse> response = analyticsQueryUseCase.getRealtimePostComments(userId, queryRequest);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
}


