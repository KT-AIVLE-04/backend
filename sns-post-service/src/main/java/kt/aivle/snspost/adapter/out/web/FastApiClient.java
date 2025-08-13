package kt.aivle.snspost.adapter.out.web;

import kt.aivle.snspost.adapter.out.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiClient {

    private final WebClient fastApiWebClient;
    private final ReactiveCircuitBreakerFactory circuitBreakerFactory;

    public Mono<FastApiPostResponse> generatePost(FastApiSnsPostRequest request) {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("generate-post");
        
        return circuitBreaker.run(
                fastApiWebClient.post()
                        .uri("/sns-post/generate-post")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(FastApiPostResponse.class)
                        .doOnSuccess(response -> log.info("FastAPI post generation successful"))
                        .doOnError(error -> log.error("FastAPI post generation failed: {}", error.getMessage())),
                throwable -> {
                    log.error("Circuit breaker opened for generate-post: {}", throwable.getMessage());
                    return Mono.error(new RuntimeException("게시물 생성 서비스가 일시적으로 사용할 수 없습니다."));
                }
        );
    }

    public Mono<FastApiHashtagResponse> generateHashtags(FastApiHashtagRequest request) {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("generate-hashtags");
        
        return circuitBreaker.run(
                fastApiWebClient.post()
                        .uri("/sns-post/generate-hashtags")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(FastApiHashtagResponse.class)
                        .doOnSuccess(response -> log.info("FastAPI hashtag generation successful"))
                        .doOnError(error -> log.error("FastAPI hashtag generation failed: {}", error.getMessage())),
                throwable -> {
                    log.error("Circuit breaker opened for generate-hashtags: {}", throwable.getMessage());
                    return Mono.error(new RuntimeException("해시태그 생성 서비스가 일시적으로 사용할 수 없습니다."));
                }
        );
    }

    public Mono<FastApiFullPostResponse> generateFullPost(FastApiSnsPostRequest request) {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("generate-full-post");
        
        return circuitBreaker.run(
                fastApiWebClient.post()
                        .uri("/sns-post/generate-full")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(FastApiFullPostResponse.class)
                        .doOnSuccess(response -> log.info("FastAPI full post generation successful"))
                        .doOnError(error -> log.error("FastAPI full post generation failed: {}", error.getMessage())),
                throwable -> {
                    log.error("Circuit breaker opened for generate-full-post: {}", throwable.getMessage());
                    return Mono.error(new RuntimeException("전체 게시물 생성 서비스가 일시적으로 사용할 수 없습니다."));
                }
        );
    }
} 