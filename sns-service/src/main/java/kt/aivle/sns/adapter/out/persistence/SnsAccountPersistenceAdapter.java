package kt.aivle.sns.adapter.out.persistence;

import kt.aivle.sns.adapter.out.persistence.repository.JpaSnsAccountRepository;
import kt.aivle.sns.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SnsAccountPersistenceAdapter implements SnsAccountRepositoryPort {

    private final JpaSnsAccountRepository snsAccountRepository;

    @Override
    public SnsAccount save(SnsAccount snsAccount) {
        return snsAccountRepository.save(snsAccount);
    }

    @Override
    public Optional<SnsAccount> findByUserIdAndStoreIdAndSnsType(Long userId, Long storeId, SnsType snsType) {
        return snsAccountRepository.findByUserIdAndSnsType(userId, snsType);
    }

    @Override
    public Optional<SnsAccount> findBySnsAccountId(String snsAccountId) {
        return snsAccountRepository.findBySnsAccountId(snsAccountId);
    }
}
