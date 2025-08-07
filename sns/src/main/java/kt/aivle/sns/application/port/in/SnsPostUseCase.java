package kt.aivle.sns.application.port.in;

import kt.aivle.sns.domain.model.PostDeleteRequest;
import kt.aivle.sns.domain.model.PostUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.domain.model.PostUploadRequest;

public interface SnsPostUseCase {
    SnsType supportSnsType();

    /**
     * post upload
     */
    void upload(String userId, PostUploadRequest request);

    /**
     * post update
     */
    void update(String userId, PostUpdateRequest request);

    /**
     * post delete
     */
    void delete(String userId, PostDeleteRequest request);
}
