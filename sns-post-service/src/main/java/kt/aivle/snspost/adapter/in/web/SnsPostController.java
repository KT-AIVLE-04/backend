// 클라이언트 요청을 처리하는 컨트롤러
package kt.aivle.snspost.adapter.in.web;

import jakarta.validation.Valid;
import kt.aivle.snspost.adapter.in.web.dto.request.HashtagRequest;
import kt.aivle.snspost.adapter.in.web.dto.request.SNSPostRequest;
import kt.aivle.snspost.adapter.in.web.dto.response.HashtagResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.SNSPostResponse;
import kt.aivle.snspost.application.port.in.SnsPostUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sns-post/agent")
@RequiredArgsConstructor
public class SnsPostController {

    private final SnsPostUseCase snsPostUseCase;

    @PostMapping("/post")
    public SNSPostResponse generatePost(@Valid @RequestBody SNSPostRequest request) {
        return snsPostUseCase.generatePost(request);
    }

    @PostMapping("/hashtags")
    public HashtagResponse generateHashtags(@Valid @RequestBody HashtagRequest request) {
        return snsPostUseCase.generateHashtags(request);
    }
}