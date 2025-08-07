package kt.aivle.sns.adapter.in.web;

import kt.aivle.sns.application.service.SnsOAuthDelegator;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sns/oauth")
@RequiredArgsConstructor
public class SnsOAuthController {
    private final SnsOAuthDelegator delegator;

    private final String TEST_USER_ID = "test-user"; // 테스트용 userId

    @GetMapping("/")
    public String home() throws Exception {
        return "sns/oauth/";
    }

    // 각 SNS 연동 버튼 누르면 호출
    @GetMapping("/{snsType}/url")
    public String getAuthUrl(@PathVariable SnsType snsType) {
        // AuthenticationPrincipal으로 userId 가져오도록?
        return delegator.getAuthUrl(snsType, TEST_USER_ID);
    }

    @GetMapping("/{snsType}/callback")
    public String callback(@PathVariable SnsType snsType, @RequestParam String code) throws Exception {
        delegator.handleCallback(snsType, TEST_USER_ID, code);

        return "계정 연동 완료";
    }
}
