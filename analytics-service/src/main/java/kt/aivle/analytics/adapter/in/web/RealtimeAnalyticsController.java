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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kt.aivle.analytics.adapter.in.web.dto.RealtimeAccountMetricsResponse;
import kt.aivle.analytics.adapter.in.web.dto.RealtimePostMetricsResponse;
import kt.aivle.analytics.adapter.in.web.validator.AnalyticsRequestValidator;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import kt.aivle.analytics.application.port.in.dto.AccountMetricsQueryRequest;
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
    private final AnalyticsRequestValidator validator;
    
    @Operation(summary = "실시간 게시물 메트릭 조회", description = "특정 게시물의 실시간 메트릭을 조회합니다.")
    @GetMapping("/posts/metrics")
    public ResponseEntity<ApiResponse<List<RealtimePostMetricsResponse>>> getRealtimePostMetrics(
            @Parameter(description = "게시물 ID (선택사항, 없으면 해당 SNS의 최근 게시물)") @RequestParam(required = false) String postId,
            @Parameter(description = "SNS 타입 (필수사항)") @RequestParam String snsType,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidUserId(userId)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_USER_ID);
        }
        
        if (!validator.isValidSnsType(snsType)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        if (postId != null && !validator.isValidPostId(postId)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
        }
        
        PostMetricsQueryRequest request;
        if (postId != null) {
            // 특정 게시물 조회
            request = PostMetricsQueryRequest.forCurrentDate(postId);
        } else {
            // 해당 SNS의 최근 게시물 조회
            request = PostMetricsQueryRequest.forLatestPostBySnsType(userId, SnsType.valueOf(snsType.toUpperCase()));
        }
        
        List<RealtimePostMetricsResponse> response = analyticsQueryUseCase.getRealtimePostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "실시간 계정 메트릭 조회", description = "특정 계정의 실시간 메트릭을 조회합니다.")
    @GetMapping("/accounts/metrics")
    public ResponseEntity<ApiResponse<RealtimeAccountMetricsResponse>> getRealtimeAccountMetrics(
            @Parameter(description = "SNS 타입 (필수사항)") @RequestParam String snsType,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidUserId(userId)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_USER_ID);
        }
        
        if (!validator.isValidSnsType(snsType)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        AccountMetricsQueryRequest request = AccountMetricsQueryRequest.forCurrentDateAndSnsType(userId, SnsType.valueOf(snsType.toUpperCase()));
        
        List<RealtimeAccountMetricsResponse> responseList = analyticsQueryUseCase.getRealtimeAccountMetrics(userId, request);
        
        // SNS 타입이 필수이므로 첫 번째 결과만 반환 (계정은 SNS 타입별로 하나씩만 존재)
        RealtimeAccountMetricsResponse response = responseList.isEmpty() ? null : responseList.get(0);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    // @Operation(summary = "실시간 게시물 댓글 조회", description = "특정 게시물의 실시간 댓글을 페이지네이션으로 조회합니다.")
    // @GetMapping("/posts/comments")
    // public ResponseEntity<ApiResponse<List<PostCommentsQueryResponse>>> getRealtimePostComments(
    //         @Parameter(description = "게시물 ID (선택사항, 없으면 해당 SNS의 최근 게시물)") @RequestParam(required = false) String postId,
    //         @Parameter(description = "SNS 타입 (필수사항)") @RequestParam String snsType,
    //         @Parameter(description = "페이지 번호 (기본값: 0)") @RequestParam(defaultValue = "0") Integer page,
    //         @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)") @RequestParam(defaultValue = "20") Integer size,
    //         @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
    //     
    //     // 입력 검증
    //     if (!validator.isValidUserId(userId)) {
    //         throw new BusinessException(AnalyticsErrorCode.INVALID_USER_ID);
    //     }
    //     
    //     if (!validator.isValidSnsType(snsType)) {
    //         throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
    //     }
    //     
    //     if (postId != null && !validator.isValidPostId(postId)) {
    //         throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
    //     }
    //     
    //     if (!validator.isValidPagination(page, size)) {
    //         throw new BusinessException(AnalyticsErrorCode.INVALID_PAGINATION);
    //     }
    //     
    //     PostCommentsQueryRequest request;
    //     if (postId != null) {
    //         request = PostCommentsQueryRequest.forCurrentDate(postId, page, size);
    //     } else {
    //         // 해당 SNS의 최근 게시물 댓글 조회
    //         request = PostCommentsQueryRequest.forLatestPostBySnsType(userId, SnsType.valueOf(snsType.toUpperCase()), page, size);
    //     }
    //     
    //     List<PostCommentsQueryResponse> response = analyticsQueryUseCase.getRealtimePostComments(userId, request);
    //     
    //     return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    // }
    

}


