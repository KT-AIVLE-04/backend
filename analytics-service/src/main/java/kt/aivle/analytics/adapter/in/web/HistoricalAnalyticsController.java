package kt.aivle.analytics.adapter.in.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import kt.aivle.analytics.adapter.in.web.dto.AccountMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.EmotionAnalysisResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostCommentsQueryResponse;
import kt.aivle.analytics.adapter.in.web.dto.PostMetricsQueryResponse;
import kt.aivle.analytics.adapter.in.web.validator.AnalyticsRequestValidator;
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
@RequestMapping("/api/analytics/history")
@RequiredArgsConstructor
@Tag(name = "Historical Analytics", description = "히스토리 분석 API")
public class HistoricalAnalyticsController {
    
    private final AnalyticsQueryUseCase analyticsQueryUseCase;
    private final AnalyticsRequestValidator validator;
    
    @Operation(summary = "히스토리 게시물 메트릭 조회", description = "특정 날짜의 게시물 메트릭 히스토리를 조회합니다.")
    @GetMapping("/posts/metrics")
    public ResponseEntity<ApiResponse<List<PostMetricsQueryResponse>>> getHistoricalPostMetrics(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식, 필수)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "SNS 타입 (필수)") @RequestParam String snsType,
            @Parameter(description = "게시물 ID (선택사항, 없으면 해당 SNS의 최근 게시물)") @RequestParam(required = false) String postId,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidUserId(userId)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_USER_ID);
        }
        
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_DATE);
        }
        
        if (!validator.isValidSnsType(snsType)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        if (postId != null && !validator.isValidPostId(postId)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
        }
        
        PostMetricsQueryRequest request;
        if (postId != null) {
            request = PostMetricsQueryRequest.forDate(date, postId);
        } else {
            // 해당 SNS의 최근 게시물 기준으로 조회
            request = PostMetricsQueryRequest.forLatestPostBySnsType(date, userId, SnsType.valueOf(snsType.toUpperCase()));
        }
        
        List<PostMetricsQueryResponse> response = analyticsQueryUseCase.getPostMetrics(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "히스토리 계정 메트릭 조회", description = "특정 날짜의 계정 메트릭 히스토리를 조회합니다.")
    @GetMapping("/accounts/metrics")
    public ResponseEntity<ApiResponse<AccountMetricsQueryResponse>> getHistoricalAccountMetrics(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식, 필수)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "SNS 타입 (필수)") @RequestParam String snsType,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidUserId(userId)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_USER_ID);
        }
        
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_DATE);
        }
        
        if (!validator.isValidSnsType(snsType)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        AccountMetricsQueryRequest request = AccountMetricsQueryRequest.forDateAndSnsType(date, userId, SnsType.valueOf(snsType.toUpperCase()));
        
        List<AccountMetricsQueryResponse> responseList = analyticsQueryUseCase.getAccountMetrics(userId, request);
        
        // SNS 타입이 필수이므로 첫 번째 결과만 반환 (계정은 SNS 타입별로 하나씩만 존재)
        AccountMetricsQueryResponse response = responseList.isEmpty() ? null : responseList.get(0);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "히스토리 게시물 댓글 조회", description = "게시물 댓글 히스토리를 페이지네이션으로 조회합니다.")
    @GetMapping("/posts/comments")
    public ResponseEntity<ApiResponse<List<PostCommentsQueryResponse>>> getHistoricalPostComments(
            @Parameter(description = "SNS 타입 (필수)") @RequestParam String snsType,
            @Parameter(description = "게시물 ID (선택사항, 없으면 해당 SNS의 최근 게시물)") @RequestParam(required = false) String postId,
            @Parameter(description = "페이지 번호 (기본값: 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기 (기본값: 20, 최대: 100)") @RequestParam(defaultValue = "20") Integer size,
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
        
        if (!validator.isValidPagination(page, size)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_PAGINATION);
        }
        
        PostCommentsQueryRequest request;
        if (postId != null) {
            // 특정 게시물 댓글 조회 (날짜 없음)
            request = PostCommentsQueryRequest.forCurrentDate(postId, page, size);
        } else {
            // 해당 SNS의 최근 게시물 댓글 조회
            request = PostCommentsQueryRequest.forLatestPostBySnsType(userId, SnsType.valueOf(snsType.toUpperCase()), page, size);
        }
        
        List<PostCommentsQueryResponse> response = analyticsQueryUseCase.getPostComments(userId, request);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
    
    @Operation(summary = "히스토리 게시물 감정분석 조회", description = "특정 날짜의 게시물 댓글 감정분석 결과와 키워드를 조회합니다.")
    @GetMapping("/posts/emotion-analysis")
    public ResponseEntity<ApiResponse<EmotionAnalysisResponse>> getHistoricalEmotionAnalysis(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식, 필수)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "SNS 타입 (필수)") @RequestParam String snsType,
            @Parameter(description = "게시물 ID (선택사항, 없으면 해당 SNS의 최근 게시물)") @RequestParam(required = false) String postId,
            @Parameter(description = "사용자 ID") @RequestHeader("X-USER-ID") String userId) {
        
        // 입력 검증
        if (!validator.isValidUserId(userId)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_USER_ID);
        }
        
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_DATE);
        }
        
        if (!validator.isValidSnsType(snsType)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_SNS_TYPE);
        }
        
        if (postId != null && !validator.isValidPostId(postId)) {
            throw new BusinessException(AnalyticsErrorCode.INVALID_POST_ID);
        }
        
        EmotionAnalysisResponse response = analyticsQueryUseCase.getHistoricalEmotionAnalysis(userId, postId, SnsType.valueOf(snsType.toUpperCase()), date);
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, response));
    }
}
