package kt.aivle.sns.application.port.out;

import kt.aivle.sns.domain.model.SnsToken;
import kt.aivle.sns.domain.model.SnsType;

import java.util.Optional;

public interface SnsTokenRepositoryPort {
    SnsToken save(SnsToken snsToken);

    Optional<SnsToken> findById(Long id);

    Optional<SnsToken> findByUserId(Long userId);

    Optional<SnsToken> findByUserIdAndSnsType(Long userId, SnsType snsType);
}
