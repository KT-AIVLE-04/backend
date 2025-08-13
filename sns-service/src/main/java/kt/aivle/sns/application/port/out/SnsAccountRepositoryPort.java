package kt.aivle.sns.application.port.out;

import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsType;

import java.util.Optional;

public interface SnsAccountRepositoryPort {
    SnsAccount save(SnsAccount snsAccount);

    Optional<SnsAccount> findByUserIdAndSnsType(Long userId, SnsType snsType);

    Optional<SnsAccount> findBySnsAccountId(String snsAccountId);
}
