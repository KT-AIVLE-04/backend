package kt.aivle.sns.application.port.in;

import kt.aivle.sns.adapter.in.web.dto.request.*;
import kt.aivle.sns.adapter.in.web.dto.response.AiPostResponse;
import kt.aivle.sns.adapter.in.web.dto.response.AiTagResponse;
import kt.aivle.sns.adapter.in.web.dto.response.PostResponse;
import kt.aivle.sns.domain.model.SnsType;

import java.util.List;

public interface SnsPostUseCase {
    SnsType supportSnsType();

    /**
     * post upload
     */
    PostResponse upload(Long userId, Long storeId, PostCreateRequest request);

    /**
     * post update
     */
    PostResponse update(Long userId, Long storeId, Long postId, PostUpdateRequest request);

    /**
     * post delete
     */
    void delete(Long userId, Long storeId, Long postId, PostDeleteRequest request);

    PostResponse get(Long userId, Long storeId, Long postId);

    List<PostResponse> getAll(Long userId, Long storeId);
}
