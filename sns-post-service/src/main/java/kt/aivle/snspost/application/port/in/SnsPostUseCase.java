package kt.aivle.snspost.application.port.in;

import kt.aivle.snspost.adapter.in.web.dto.request.HashtagRequest;
import kt.aivle.snspost.adapter.in.web.dto.request.SNSPostRequest;
import kt.aivle.snspost.adapter.in.web.dto.response.HashtagResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.SNSPostResponse;

public interface SnsPostUseCase {

    SNSPostResponse generatePost(SNSPostRequest request);

    HashtagResponse generateHashtags(HashtagRequest request);
} 