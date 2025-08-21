// FastApiClient에서 호출하는 외부 FastAPI 서버 경로 (Spring Boot → FastAPI)
package kt.aivle.sns.adapter.out.web;

import kt.aivle.sns.adapter.in.web.dto.request.CreateHashtagRequest;
import kt.aivle.sns.adapter.in.web.dto.request.CreatePostRequest;
import kt.aivle.sns.adapter.in.web.dto.response.CreateHashtagResponse;
import kt.aivle.sns.adapter.in.web.dto.response.CreatePostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiClient {

    private final WebClient fastApiWebClient;

    public CreatePostResponse createPost(CreatePostRequest request) {
        try {
            return fastApiWebClient.post()
                    .uri("/sns-post/agent/post")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CreatePostResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("FastAPI 게시물 생성 실패: {}", e.getMessage());
            throw new RuntimeException("게시물 생성에 실패했습니다.");
        }
    }

    public CreateHashtagResponse createHashtags(CreateHashtagRequest request) {
        try {
            return fastApiWebClient.post()
                    .uri("/sns-post/agent/hashtags")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CreateHashtagResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("FastAPI 해시태그 생성 실패: {}", e.getMessage());
            throw new RuntimeException("해시태그 생성에 실패했습니다.");
        }
    }
} 