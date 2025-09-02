package kt.aivle.sns.adapter.in.web;

import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.sns.adapter.in.web.dto.OAuthContext;
import kt.aivle.sns.application.service.AccountSyncDelegator;
import kt.aivle.sns.application.service.SnsOAuthDelegator;
import kt.aivle.sns.application.service.oauth.OAuthTemplateService;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/sns/oauth")
@RequiredArgsConstructor
public class SnsOAuthController {

    private final SnsOAuthDelegator delegator;
    private final AccountSyncDelegator syncDelegator;
    private final ResponseUtils responseUtils;
    private final OAuthTemplateService templateService;

    // 각 SNS 연동 버튼 누르면 호출
    @GetMapping("/{snsType}/login")
    public ResponseEntity<ApiResponse<String>> getAuthUrl(@PathVariable SnsType snsType,
                                                          @RequestHeader("X-USER-ID") Long userId,
                                                          @RequestHeader("X-STORE-ID") Long storeId) {
        String authUrl = delegator.getAuthUrl(snsType, userId, storeId);
        return responseUtils.build(OK, authUrl);
    }

    @GetMapping("/{snsType}/callback")
    public ResponseEntity<String> callback(@PathVariable SnsType snsType,
                                                        @RequestParam String code,
                                                        @RequestParam String state) {
        try {
            OAuthContext ctx = delegator.handleCallback(snsType, state, code);
            // account 초기화
            syncDelegator.accountSync(snsType, ctx.userId(), ctx.storeId());
            
            log.info("SNS OAuth 연동 성공: snsType={}, userId={}, storeId={}", 
                    snsType, ctx.userId(), ctx.storeId());
            
            String successHtml = templateService.createSuccessHtml(snsType);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(successHtml);
                    
        } catch (Exception e) {
            log.error("SNS OAuth 연동 실패: snsType={}, error={}", snsType, e.getMessage());
            
            String errorHtml = templateService.createErrorHtml(snsType, e.getMessage());
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(errorHtml);
        }
    }
}