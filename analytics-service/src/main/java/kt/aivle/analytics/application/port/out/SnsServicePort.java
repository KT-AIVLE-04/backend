package kt.aivle.analytics.application.port.out;

import kt.aivle.analytics.adapter.in.event.dto.PostInfoResponseMessage;
import reactor.core.publisher.Mono;

public interface SnsServicePort {
    Mono<PostInfoResponseMessage> getPostInfo(Long postId, Long userId, Long accountId, Long storeId);
}
