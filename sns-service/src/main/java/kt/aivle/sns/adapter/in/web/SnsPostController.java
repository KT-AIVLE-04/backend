package kt.aivle.sns.adapter.in.web;

import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.sns.adapter.in.web.dto.request.PostCreateRequest;
import kt.aivle.sns.adapter.in.web.dto.request.PostDeleteRequest;
import kt.aivle.sns.adapter.in.web.dto.response.PostResponse;
import kt.aivle.sns.application.port.in.SnsPostUseCase;
import kt.aivle.sns.application.service.SnsPostDelegator;
import kt.aivle.sns.infra.CloudFrontSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;

import java.util.List;

import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/sns/posts")
@RequiredArgsConstructor
public class SnsPostController {

    private final SnsPostDelegator snsPostDelegator;
    private final SnsPostUseCase snsPostUseCase;
    private final CloudFrontSigner cloudFrontSigner;
    private final BuildCookie buildCookie;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> uploadPost(@RequestHeader("X-USER-ID") Long userId,
                                                                @RequestHeader("X-STORE-ID") Long storeId,
                                                                @RequestBody PostCreateRequest request) {
        PostResponse response = snsPostDelegator.upload(userId, storeId, request);
        return responseUtils.build(OK, response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@RequestHeader("X-USER-ID") Long userId,
                                                         @RequestHeader("X-STORE-ID") Long storeId,
                                                         @PathVariable Long id,
                                                         @RequestBody PostDeleteRequest request) {
        snsPostDelegator.delete(userId, storeId, id, request);
        return responseUtils.build(OK, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@RequestHeader("X-USER-ID") Long userId,
                                                             @RequestHeader("X-STORE-ID") Long storeId,
                                                             @PathVariable Long id) {
        PostResponse response = snsPostUseCase.get(userId, storeId, id);
        return responseUtils.build(OK, response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPosts(@RequestHeader("X-USER-ID") Long userId,
                                                                    @RequestHeader("X-STORE-ID") Long storeId) {

        String keyPrefix = thumbPrefix(userId, storeId);
        String cookiePath = "/" + keyPrefix;

        CookiesForCustomPolicy cookies = cloudFrontSigner.issueThumbCookiesFor(keyPrefix);

        String sig = buildCookie.buildCfCookieHeader(cookies.signatureHeaderValue(), cookiePath);
        String kpid = buildCookie.buildCfCookieHeader(cookies.keyPairIdHeaderValue(), cookiePath);
        String pol = buildCookie.buildCfCookieHeader(cookies.policyHeaderValue(), cookiePath);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, sig);
        headers.add(HttpHeaders.SET_COOKIE, kpid);
        headers.add(HttpHeaders.SET_COOKIE, pol);

        List<PostResponse> response = snsPostUseCase.getAll(userId, storeId);

        return responseUtils.build(OK, response, headers);
    }

    public String thumbPrefix(Long userId, Long storeId) {
        return String.format("thumbnail/%d-%d/", userId, storeId);
    }

    //    @PatchMapping("/{id}")
//    public ResponseEntity<ApiResponse<PostResponse>> updateVideo(@RequestHeader("X-USER-ID") Long userId,
//                                                                 @RequestHeader("X-STORE-ID") Long storeId,
//                                                                 @PathVariable Long id,
//                                                                 @RequestBody PostUpdateRequest request) {
//        PostResponse response = snsPostDelegator.update(userId, storeId, id, request);
//        return responseUtils.build(OK, response);
//    }
}