package kt.aivle.sns.adapter.out.persistence;

import kt.aivle.sns.domain.model.SnsToken;
import kt.aivle.sns.domain.model.SnsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnsTokenRepository extends JpaRepository<SnsToken, Long> {
    Optional<SnsToken> findByUserId(String userId);

    Optional<SnsToken> findByUserIdAndSnsType(String userId, SnsType snsType);
}
