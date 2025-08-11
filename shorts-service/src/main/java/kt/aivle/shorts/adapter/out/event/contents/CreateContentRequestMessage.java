package kt.aivle.shorts.adapter.out.event.contents;

import java.util.List;

public record CreateContentRequestMessage(Long storeId, List<ImageItem> images) {
    public record ImageItem(
            String url,
            String s3Key,
            String originalName,
            String contentType) {
    }
}