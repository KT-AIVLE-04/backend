package kt.aivle.snspost.application.port.in;

import kt.aivle.snspost.adapter.in.web.dto.request.GenerateHashtagRequest;
import kt.aivle.snspost.adapter.in.web.dto.request.GeneratePostRequest;
import kt.aivle.snspost.adapter.in.web.dto.response.FullPostResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.HashtagResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.PostResponse;
import reactor.core.publisher.Mono;

public interface SnsPostUseCase {

    Mono<PostResponse> generatePost(GeneratePostRequest request, Long userId, Long storeId);

    Mono<HashtagResponse> generateHashtags(GenerateHashtagRequest request);

    Mono<FullPostResponse> generateFullPost(GeneratePostRequest request, Long userId, Long storeId);
} 