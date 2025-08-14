package kt.aivle.content.dto;

import kt.aivle.content.entity.Content;
import org.mapstruct.Mapper;
import org.springframework.web.multipart.MultipartFile;

@Mapper(componentModel = "spring")
public interface ContentMapper {

    CreateContentRequest toCreateContentRequest(Long userId, Long storeId, MultipartFile file);

    ContentResponse toContentResponse(Content content);
}
