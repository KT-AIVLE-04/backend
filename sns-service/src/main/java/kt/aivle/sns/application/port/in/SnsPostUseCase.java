package kt.aivle.sns.application.port.in;

import kt.aivle.sns.adapter.in.web.dto.PostDeleteRequest;
import kt.aivle.sns.adapter.in.web.dto.PostUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.adapter.in.web.dto.PostUploadRequest;

public interface SnsPostUseCase {
    SnsType supportSnsType();

    /**
     * post upload
     */
    void upload(Long userId, PostUploadRequest request);

    /**
     * post update
     */
    void update(Long userId, PostUpdateRequest request);

    /**
     * post delete
     */
    void delete(Long userId, PostDeleteRequest request);
}
