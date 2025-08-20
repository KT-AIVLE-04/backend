package kt.aivle.sns.adapter.in.web;

import kt.aivle.sns.adapter.in.web.dto.OAuthContext;
import kt.aivle.sns.application.service.AccountSyncDelegator;
import kt.aivle.sns.application.service.SnsOAuthDelegator;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/sns/oauth")
@RequiredArgsConstructor
public class SnsOAuthController {
    private final SnsOAuthDelegator delegator;
    private final AccountSyncDelegator syncDelegator;

    @GetMapping("/")
    public String home() throws Exception {
        return "sns/oauth/";
    }

    // 각 SNS 연동 버튼 누르면 호출
    @GetMapping("/{snsType}/url")
    public String getAuthUrl(@PathVariable SnsType snsType,
                             @RequestHeader("X-USER-ID") Long userId,
                             @RequestHeader("X-STORE-ID") Long storeId) {
        return delegator.getAuthUrl(snsType, userId, storeId);
    }

    @GetMapping("/{snsType}/callback")
    public String callback(@PathVariable SnsType snsType,
                           @RequestParam String code,
                           @RequestParam String state) throws Exception {
        OAuthContext ctx = delegator.handleCallback(snsType, state, code);
        // account 초기화
        syncDelegator.accountSync(snsType, ctx.getUserId(), ctx.getStoreId());

        return "계정 연동 완료";
    }
}
