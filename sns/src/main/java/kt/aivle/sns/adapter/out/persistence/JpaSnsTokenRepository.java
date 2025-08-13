package kt.aivle.sns.adapter.out.persistence;

import kt.aivle.sns.domain.model.SnsToken;
import kt.aivle.sns.domain.model.SnsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaSnsTokenRepository extends JpaRepository<SnsToken, Long> {
    Optional<SnsToken> findByUserId(Long userId);

    Optional<SnsToken> findByUserIdAndSnsType(Long userId, SnsType snsType);
}
