package kt.aivle.content.service;

import kt.aivle.content.dto.request.*;
import kt.aivle.content.dto.response.ContentDetailResponse;
import kt.aivle.content.dto.response.ContentResponse;
import kt.aivle.content.event.CreateContentRequestMessage;

import java.util.List;

public interface ContentService {

    ContentResponse uploadContent(CreateContentRequest request);

    void uploadContent(CreateContentRequestMessage request);

    ContentDetailResponse getContentDetail(GetContentRequest request);

    List<ContentResponse> getContents(GetContentListRequest request);

    ContentDetailResponse updateContent(UpdateContentRequest request);

    void deleteContent(DeleteContentRequest request);
}
