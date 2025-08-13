package kt.aivle.shorts.adapter.out.s3.mapper;

import kt.aivle.shorts.adapter.out.s3.dto.UploadedObject;
import kt.aivle.shorts.application.port.out.s3.UploadedObjectResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface S3ImageMapper {
    UploadedObjectResponse toUploadImageResponse(UploadedObject uploadedObject);
}
