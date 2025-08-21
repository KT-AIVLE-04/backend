package kt.aivle.sns.application.port.in;

import kt.aivle.sns.domain.model.SnsType;

public interface AccountSyncUseCase {
    SnsType supportSnsType();

    void accountSync(SnsType snsType, Long userId, Long storeId);
}
