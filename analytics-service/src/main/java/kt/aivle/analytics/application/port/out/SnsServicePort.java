package kt.aivle.analytics.application.port.out;

import java.util.concurrent.CompletableFuture;

import kt.aivle.analytics.adapter.in.event.dto.PostInfoResponseMessage;

public interface SnsServicePort {
    CompletableFuture<PostInfoResponseMessage> getPostInfo(Long postId, Long userId, Long accountId, Long storeId);
}
