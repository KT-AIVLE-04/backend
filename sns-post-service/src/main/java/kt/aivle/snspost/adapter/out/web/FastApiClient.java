// FastAPI 서버로 요청을 보내는 클라이언트
package kt.aivle.snspost.adapter.out.web;

import kt.aivle.snspost.adapter.in.web.dto.request.HashtagRequest;
import kt.aivle.snspost.adapter.in.web.dto.request.SNSPostRequest;
import kt.aivle.snspost.adapter.in.web.dto.response.HashtagResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.SNSPostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiClient {

    private final WebClient fastApiWebClient;

    public SNSPostResponse generatePost(SNSPostRequest request) {
        try {
            return fastApiWebClient.post()
                    .uri("/sns-post/agent/post")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SNSPostResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("FastAPI 게시물 생성 실패: {}", e.getMessage());
            throw new RuntimeException("게시물 생성에 실패했습니다.");
        }
    }

    public HashtagResponse generateHashtags(HashtagRequest request) {
        try {
            return fastApiWebClient.post()
                    .uri("/sns-post/agent/hashtags")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(HashtagResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("FastAPI 해시태그 생성 실패: {}", e.getMessage());
            throw new RuntimeException("해시태그 생성에 실패했습니다.");
        }
    }
} 