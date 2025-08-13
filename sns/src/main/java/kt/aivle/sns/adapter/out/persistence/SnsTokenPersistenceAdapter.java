package kt.aivle.sns.adapter.out.persistence;

import kt.aivle.sns.application.port.out.SnsTokenRepositoryPort;
import kt.aivle.sns.domain.model.SnsToken;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SnsTokenPersistenceAdapter implements SnsTokenRepositoryPort {

    private final JpaSnsTokenRepository snsTokenRepository;

    @Override
    public SnsToken save(SnsToken snsToken) {
        return snsTokenRepository.save(snsToken);
    }

    @Override
    public Optional<SnsToken> findById(Long id) {
        return snsTokenRepository.findById(id);
    }

    @Override
    public Optional<SnsToken> findByUserId(Long userId) {
        return snsTokenRepository.findByUserId(userId);
    }

    @Override
    public Optional<SnsToken> findByUserIdAndSnsType(Long userId, SnsType snsType) {
        return snsTokenRepository.findByUserIdAndSnsType(userId, snsType);
    }
}
