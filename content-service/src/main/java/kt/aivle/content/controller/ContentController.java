package kt.aivle.content.controller;

import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.content.dto.ContentMapper;
import kt.aivle.content.dto.request.*;
import kt.aivle.content.dto.response.ContentDetailResponse;
import kt.aivle.content.dto.response.ContentResponse;
import kt.aivle.content.infra.CloudFrontSigner;
import kt.aivle.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;

import java.util.List;

import static kt.aivle.common.code.CommonResponseCode.CREATED;
import static kt.aivle.common.code.CommonResponseCode.OK;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;
    private final ContentMapper contentMapper;
    private final CloudFrontSigner cloudFrontSigner;
    private final BuildCookie buildCookie;
    private final ResponseUtils responseUtils;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ContentResponse>> createContent(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @RequestPart("file") MultipartFile file
    ) {
        CreateContentRequest request = contentMapper.toCreateContentRequest(userId, storeId, file);
        ContentResponse response = contentService.uploadContent(request);
        return responseUtils.build(CREATED, response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentDetailResponse>> getContent(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @PathVariable Long id) {
        GetContentRequest request = contentMapper.toGetContentRequest(id, userId, storeId);
        ContentDetailResponse response = contentService.getContentDetail(request);
        return responseUtils.build(OK, response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentResponse>>> listContents(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @RequestParam(required = false) String query
    ) {

        String keyPrefix = thumbPrefix(userId, storeId);
        String cookiePath = "/" + keyPrefix;

        CookiesForCustomPolicy cookies = cloudFrontSigner.issueThumbCookiesFor(keyPrefix);

        GetContentListRequest request = contentMapper.toGetContentListRequest(userId, storeId, query);
        List<ContentResponse> response = contentService.getContents(request);

        ResponseCookie sig = buildCookie.buildCfCookie(cookies.signatureHeaderValue(), cookiePath);
        ResponseCookie kpid = buildCookie.buildCfCookie(cookies.keyPairIdHeaderValue(), cookiePath);
        ResponseCookie pol = buildCookie.buildCfCookie(cookies.policyHeaderValue(), cookiePath);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, sig.toString());
        headers.add(HttpHeaders.SET_COOKIE, kpid.toString());
        headers.add(HttpHeaders.SET_COOKIE, pol.toString());

        return responseUtils.build(OK, response, headers);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentDetailResponse>> updateTitle(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @PathVariable Long id,
            @RequestParam String title
    ) {
        UpdateContentRequest request = contentMapper.toUpdateContentRequest(id, userId, storeId, title);
        ContentDetailResponse response = contentService.updateContent(request);
        return responseUtils.build(OK, response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @PathVariable Long id) {
        DeleteContentRequest request = contentMapper.toDeleteContentRequest(id, userId, storeId);
        contentService.deleteContent(request);
        return responseUtils.build(OK, null);
    }

    public String thumbPrefix(Long userId, Long storeId) {
        return String.format("thumbnail/%d-%d/", userId, storeId);
    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        service.delete(id);
//        return ResponseEntity.noContent().build();
//    }
}