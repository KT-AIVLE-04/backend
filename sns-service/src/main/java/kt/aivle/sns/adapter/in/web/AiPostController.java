// adapter/in/web/AIPostController.java
// Spring Boot 서버 내 REST API 경로 (클라이언트 → Spring Boot)
package kt.aivle.sns.adapter.in.web;

import jakarta.validation.Valid;
import kt.aivle.sns.adapter.in.web.dto.request.CreateHashtagRequest;
import kt.aivle.sns.adapter.in.web.dto.request.CreatePostRequest;
import kt.aivle.sns.adapter.in.web.dto.response.CreateHashtagResponse;
import kt.aivle.sns.adapter.in.web.dto.response.CreatePostResponse;
import kt.aivle.sns.application.port.in.AiPostUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/ai")  
@RequiredArgsConstructor
public class AiPostController {

    private final AiPostUseCase aiPostUseCase;

    @PostMapping("/post")
    public CreatePostResponse createPost(@Valid @RequestBody CreatePostRequest request) {
        return aiPostUseCase.createPost(request);
    }

    @PostMapping("/hashtags")
    public CreateHashtagResponse createHashtags(@Valid @RequestBody CreateHashtagRequest request) {
        return aiPostUseCase.createHashtags(request);
    }
}