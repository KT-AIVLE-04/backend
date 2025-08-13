package kt.aivle.snspost.application.service.mapper;

import kt.aivle.snspost.adapter.in.web.dto.request.GenerateHashtagRequest;
import kt.aivle.snspost.adapter.in.web.dto.request.GeneratePostRequest;
import kt.aivle.snspost.adapter.in.web.dto.response.FullPostResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.HashtagResponse;
import kt.aivle.snspost.adapter.in.web.dto.response.PostResponse;
import kt.aivle.snspost.adapter.out.web.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SnsPostMapper {

    @Mapping(target = "contentType", expression = "java(request.getContentType().getValue())")
    @Mapping(target = "snsPlatform", expression = "java(request.getSnsPlatform().getValue())")
    FastApiSnsPostRequest toFastApiSnsPostRequest(GeneratePostRequest request);

    @Mapping(target = "snsPlatform", expression = "java(request.getSnsPlatform().getValue())")
    FastApiHashtagRequest toFastApiHashtagRequest(GenerateHashtagRequest request);

    PostResponse toPostResponse(FastApiPostResponse fastApiResponse);

    HashtagResponse toHashtagResponse(FastApiHashtagResponse fastApiResponse);

    @Mapping(target = "post", source = "fastApiResponse.post")
    @Mapping(target = "hashtags", source = "fastApiResponse.hashtags")
    FullPostResponse toFullPostResponse(FastApiFullPostResponse fastApiResponse);
} 