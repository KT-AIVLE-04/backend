package kt.aivle.sns.application.port.in;

import kt.aivle.sns.adapter.in.web.dto.SnsAccountUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;

public interface SnsAccountUseCase {
    SnsType supportSnsType();

    void getSnsAccountInfo(Long userId, Long storeId);

    void updateSnsAccount(Long userId, SnsAccountUpdateRequest request);

    void getPostList(Long userId, Long storeId);
}
