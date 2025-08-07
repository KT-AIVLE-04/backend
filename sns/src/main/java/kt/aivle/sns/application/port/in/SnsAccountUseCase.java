package kt.aivle.sns.application.port.in;

import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsAccountUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;

public interface SnsAccountUseCase {
    SnsType supportSnsType();

    void getSnsAccountInfo(String userId);

    void updateSnsAccount(String userId, SnsAccountUpdateRequest request);

    void getPostList(String userId);
}
