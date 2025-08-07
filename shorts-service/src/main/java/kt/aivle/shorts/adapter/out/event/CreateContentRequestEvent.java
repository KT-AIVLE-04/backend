package kt.aivle.shorts.adapter.out.event;

import kt.aivle.shorts.adapter.out.s3.UploadedImageInfo;

import java.util.List;

public record CreateContentRequestEvent(
        Long storeId,
        List<UploadedImageInfo> uploadedImageInfos) {
}