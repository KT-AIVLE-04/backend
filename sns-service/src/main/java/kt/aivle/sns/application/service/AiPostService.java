package kt.aivle.sns.application.service;

import kt.aivle.sns.adapter.in.web.dto.request.CreateHashtagRequest;
import kt.aivle.sns.adapter.in.web.dto.request.CreatePostRequest;
import kt.aivle.sns.adapter.in.web.dto.response.CreateHashtagResponse;
import kt.aivle.sns.adapter.in.web.dto.response.CreatePostResponse;
import kt.aivle.sns.adapter.out.web.FastApiClient;
import kt.aivle.sns.application.port.in.AiPostUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPostService implements AiPostUseCase {

    private final FastApiClient fastApiClient;

    @Override
    public CreatePostResponse createPost(CreatePostRequest request) {
        return fastApiClient.createPost(request);
    }

    @Override
    public CreateHashtagResponse createHashtags(CreateHashtagRequest request) {
        return fastApiClient.createHashtags(request);
    }
} 