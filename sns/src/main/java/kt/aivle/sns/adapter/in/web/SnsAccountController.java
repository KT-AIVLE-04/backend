package kt.aivle.sns.adapter.in.web;

import kt.aivle.sns.application.service.SnsAccountDelegator;
import kt.aivle.sns.domain.model.SnsAccountUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sns/account")
@RequiredArgsConstructor
public class SnsAccountController {

    private final SnsAccountDelegator snsAccountDelegator;

    //AuthenticatonPrincipal?
    //SecurityContext?
    private final String TEST_USER_ID = "test-user"; // 테스트용 userId

    @GetMapping("/{snsType}")
    public ResponseEntity<?> getAccountInfo(@PathVariable SnsType snsType) {
        snsAccountDelegator.getAccountInfo(snsType, TEST_USER_ID);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{snsType}")
    public ResponseEntity<?> updateAccount(@PathVariable SnsType snsType, @RequestBody SnsAccountUpdateRequest request) {
        snsAccountDelegator.updateAccount(snsType, TEST_USER_ID, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{snsType}/list")
    public ResponseEntity<?> getPostList(@PathVariable SnsType snsType) {
        snsAccountDelegator.getPostList(snsType, TEST_USER_ID);
        return ResponseEntity.ok().build();
    }
}
