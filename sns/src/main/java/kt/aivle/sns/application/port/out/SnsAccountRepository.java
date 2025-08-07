package kt.aivle.sns.application.port.out;

import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnsAccountRepository extends JpaRepository<SnsAccount, Long> {

    Optional<SnsAccount> findByUserIdAndSnsType(String userId, SnsType snsType);

    Optional<SnsAccount> findBySnsAccountId(String snsAccountId);
}
