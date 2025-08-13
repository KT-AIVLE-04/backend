package kt.aivle.snspost.adapter.in.web;

import jakarta.validation.Valid;
import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.snspost.adapter.in.web.dto.request.GenerateHashtagRequest;
import kt.aivle.snspost.adapter.in.web.dto.request.GeneratePostRequest;
import kt.aivle.snspost.adapter.in.web.dto.response.FullPostResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.HashtagResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.PostResponse;
import kt.aivle.snspost.application.port.in.SnsPostUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/sns-posts")
@RequiredArgsConstructor
public class SnsPostController {

    private final SnsPostUseCase snsPostUseCase;
    private final ResponseUtils responseUtils;

    @PostMapping("/generate-post")
    public Mono<ResponseEntity<ApiResponse<PostResponse>>> generatePost(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @Valid @RequestBody GeneratePostRequest request
    ) {
        return snsPostUseCase.generatePost(request, userId, storeId)
                .map(response -> responseUtils.build(OK, response));
    }

    @PostMapping("/generate-hashtags")
    public Mono<ResponseEntity<ApiResponse<HashtagResponse>>> generateHashtags(
            @Valid @RequestBody GenerateHashtagRequest request
    ) {
        return snsPostUseCase.generateHashtags(request)
                .map(response -> responseUtils.build(OK, response));
    }

    @PostMapping("/generate-full")
    public Mono<ResponseEntity<ApiResponse<FullPostResponse>>> generateFullPost(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @Valid @RequestBody GeneratePostRequest request
    ) {
        return snsPostUseCase.generateFullPost(request, userId, storeId)
                .map(response -> responseUtils.build(OK, response));
    }
} 