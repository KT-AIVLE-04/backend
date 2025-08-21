package kt.aivle.sns.application.service;

import kt.aivle.sns.adapter.in.web.dto.request.AiPostCreateRequest;
import kt.aivle.sns.adapter.in.web.dto.request.AiTagCreateRequest;
import kt.aivle.sns.adapter.in.web.dto.response.AiPostResponse;
import kt.aivle.sns.adapter.in.web.dto.response.AiTagResponse;
import kt.aivle.sns.application.port.in.AiSnsUseCase;
import kt.aivle.sns.infra.S3Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiSnsPostService implements AiSnsUseCase {

    private final RestTemplate restTemplate;
    private final S3Storage s3Storage;

    @Override
    public AiPostResponse createAiPost(AiPostCreateRequest request) {
        String originKey = getOriginKey(request.objectKey(), request.originalName());
        String presignUrl = s3Storage.getPresignUrl(originKey);
        Map<String, Object> body = Map.of(
                "content_data", presignUrl,
                "user_keywords", request.keywords(),
                "sns_platform", request.snsType(),
                "business_type", request.industry(),
                "location", request.location()
        );

        AiPostResponse.AiSnakeCaseResponse aiResponse = restTemplate.postForObject(
                "/sns-post/agent/post",
                body,
                AiPostResponse.AiSnakeCaseResponse.class
        );

        return AiPostResponse.from(aiResponse);
    }

    @Override
    public AiTagResponse createAiTag(AiTagCreateRequest request) {
        Map<String, Object> body = Map.of(
                "post_title", request.title(),
                "post_content", request.description(),
                "user_keywords", request.keywords(),
                "sns_platform", request.snsType(),
                "business_type", request.industry(),
                "location", request.location()
        );

        AiTagResponse.AiSnakeCaseResponse aiResponse = restTemplate.postForObject(
                "/sns-post/agent/tag",
                body,
                AiTagResponse.AiSnakeCaseResponse.class
        );

        return AiTagResponse.from(aiResponse);
    }

    private String getOriginKey(String uuid, String originalFilename) {
        return "origin/" + uuid + "-" + originalFilename;
    }
}
