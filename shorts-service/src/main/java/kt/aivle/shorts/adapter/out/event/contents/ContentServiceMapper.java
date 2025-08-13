package kt.aivle.shorts.adapter.out.event.contents;

import kt.aivle.shorts.application.port.out.event.contents.CreateContentRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContentServiceMapper {
    CreateContentRequestMessage toCreateContentRequestMessage(CreateContentRequest request);
}
