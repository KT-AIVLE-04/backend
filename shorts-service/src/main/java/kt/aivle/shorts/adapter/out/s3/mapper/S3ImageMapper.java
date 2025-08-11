package kt.aivle.shorts.adapter.out.s3.mapper;

import kt.aivle.shorts.adapter.out.s3.dto.DeleteS3ImageRequest;
import kt.aivle.shorts.adapter.out.s3.dto.UploadedImageInfo;
import kt.aivle.shorts.application.port.out.s3.DeleteImageRequest;
import kt.aivle.shorts.application.port.out.s3.UploadImageResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface S3ImageMapper {
    UploadImageResponse toUploadImageResponse(UploadedImageInfo uploadedImageInfo);

    DeleteS3ImageRequest toDeleteS3ImageRequest(DeleteImageRequest request);
}
