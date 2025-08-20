package kt.aivle.sns.adapter.out.persistence;

import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaSnsAccountRepository extends JpaRepository<SnsAccount, Long> {

    Optional<SnsAccount> findByUserIdAndSnsType(Long userId, SnsType snsType);

    Optional<SnsAccount> findBySnsAccountId(String snsAccountId);
}
