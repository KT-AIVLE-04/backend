package kt.aivle.sns.adapter.in.web;

import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.sns.adapter.in.web.dto.SnsAccountResponse;
import kt.aivle.sns.application.service.SnsAccountDelegator;
import kt.aivle.sns.adapter.in.web.dto.SnsAccountUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/sns/account")
@RequiredArgsConstructor
public class SnsAccountController {

    private final SnsAccountDelegator snsAccountDelegator;

    private final ResponseUtils responseUtils;

    @GetMapping("/{snsType}")
    public ResponseEntity<ApiResponse<SnsAccountResponse>> getAccountInfo(@PathVariable SnsType snsType,
                                                      @RequestHeader("X-USER-ID") Long userId,
                                                      @RequestParam Long storeId) {
        SnsAccountResponse account = snsAccountDelegator.getAccountInfo(snsType, userId, storeId);
        return responseUtils.build(OK, account);
    }

    @PutMapping("/{snsType}")
    public ResponseEntity<?> updateAccount(@PathVariable SnsType snsType,
                                           @RequestHeader("X-USER-ID") Long userId,
                                           @RequestBody SnsAccountUpdateRequest request) {
        snsAccountDelegator.updateAccount(snsType, userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{snsType}/list")
    public ResponseEntity<?> getPostList(@PathVariable SnsType snsType,
                                         @RequestHeader("X-USER-ID") Long userId,
                                         @RequestParam Long storeId) {
        snsAccountDelegator.getPostList(snsType, userId, storeId);
        return ResponseEntity.ok().build();
    }
}
