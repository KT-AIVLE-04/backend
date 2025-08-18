package kt.aivle.content.dto;

import kt.aivle.content.dto.request.*;
import kt.aivle.content.dto.response.ContentDetailResponse;
import kt.aivle.content.dto.response.ContentResponse;
import kt.aivle.content.entity.Content;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.multipart.MultipartFile;

@Mapper(componentModel = "spring")
public interface ContentMapper {

    /**
     * request
     **/
    CreateContentRequest toCreateContentRequest(Long userId, Long storeId, MultipartFile file);

    GetContentRequest toGetContentRequest(Long id, Long userId, Long storeId);

    UpdateContentRequest toUpdateContentRequest(Long id, Long userId, Long storeId, String title);

    DeleteContentRequest toDeleteContentRequest(Long id, Long userId, Long storeId);

    GetContentListRequest toGetContentListRequest(Long userId, Long storeId, String query);

    /**
     * response
     **/

    ContentResponse toContentResponse(Content content);

    ContentResponse toContentResponse(Content content, String url);

    @Mapping(target = "bytes", source = "content.bytes")
    ContentDetailResponse toContentDetailResponse(Content content, String url);
}