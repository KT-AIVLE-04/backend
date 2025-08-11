package kt.aivle.shorts.application.port.out.event.contents;

import java.util.List;

public record CreateContentRequest(Long storeId, List<ImageItem> images) {
    public record ImageItem(
            String url,
            String s3Key,
            String originalName,
            String contentType) {
    }
}
