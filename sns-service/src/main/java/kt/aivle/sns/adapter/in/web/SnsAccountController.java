package kt.aivle.sns.adapter.in.web;

import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.sns.adapter.in.web.dto.request.SnsAccountUpdateRequest;
import kt.aivle.sns.adapter.in.web.dto.response.SnsAccountResponse;
import kt.aivle.sns.application.service.SnsAccountDelegator;
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
                                                                          @RequestHeader("X-STORE-ID") Long storeId) {
        SnsAccountResponse account = snsAccountDelegator.getAccountInfo(snsType, userId, storeId);
        return responseUtils.build(OK, account);
    }

    @PutMapping("/{snsType}")
    public ResponseEntity<ApiResponse<Void>> updateAccount(@PathVariable SnsType snsType,
                                                           @RequestHeader("X-USER-ID") Long userId,
                                                           @RequestBody SnsAccountUpdateRequest request) {
        snsAccountDelegator.updateAccount(snsType, userId, request);
        return responseUtils.build(OK, null);
    }

//    @GetMapping("/{snsType}/list")
//    public ResponseEntity<?> getPostList(@PathVariable SnsType snsType,
//                                         @RequestHeader("X-USER-ID") Long userId,
//                                         @RequestHeader("X-STORE-ID") Long storeId) {
//        snsAccountDelegator.getPostList(snsType, userId, storeId);
//        return responseUtils.build(OK, null);
//    }
}