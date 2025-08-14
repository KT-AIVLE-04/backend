package kt.aivle.snspost.application.service;

import kt.aivle.snspost.adapter.in.web.dto.request.HashtagRequest;
import kt.aivle.snspost.adapter.in.web.dto.request.SNSPostRequest;
import kt.aivle.snspost.adapter.in.web.dto.response.HashtagResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.SNSPostResponse;
import kt.aivle.snspost.adapter.out.web.FastApiClient;
import kt.aivle.snspost.application.port.in.SnsPostUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SnsPostService implements SnsPostUseCase {

    private final FastApiClient fastApiClient;

    @Override
    public SNSPostResponse generatePost(SNSPostRequest request) {
        return fastApiClient.generatePost(request);
    }

    @Override
    public HashtagResponse generateHashtags(HashtagRequest request) {
        return fastApiClient.generateHashtags(request);
    }
} 