package kt.aivle.sns.application.port.in;

import kt.aivle.sns.adapter.in.web.dto.request.CreateHashtagRequest;
import kt.aivle.sns.adapter.in.web.dto.request.CreatePostRequest;
import kt.aivle.sns.adapter.in.web.dto.response.CreateHashtagResponse;
import kt.aivle.sns.adapter.in.web.dto.response.CreatePostResponse;

public interface AiPostUseCase {

    CreatePostResponse createPost(CreatePostRequest request);

    CreateHashtagResponse createHashtags(CreateHashtagRequest request);
} 