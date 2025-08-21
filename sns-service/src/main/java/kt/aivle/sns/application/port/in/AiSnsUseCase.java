package kt.aivle.sns.application.port.in;

import kt.aivle.sns.adapter.in.web.dto.request.AiPostCreateRequest;
import kt.aivle.sns.adapter.in.web.dto.request.AiTagCreateRequest;
import kt.aivle.sns.adapter.in.web.dto.response.AiPostResponse;
import kt.aivle.sns.adapter.in.web.dto.response.AiTagResponse;

public interface AiSnsUseCase {

    AiPostResponse createAiPost(AiPostCreateRequest request);

    AiTagResponse createAiTag(AiTagCreateRequest request);
}