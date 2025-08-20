package kt.aivle.sns.application.service.youtube;

import kt.aivle.sns.application.port.out.SnsTokenRepositoryPort;
import kt.aivle.sns.domain.model.SnsToken;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YoutubeTokenService {
    private final SnsTokenRepositoryPort snsTokenRepositoryPort;

    public void saveToken(Long userId,
                          Long storeId,
                          String accessToken,
                          String refreshToken,
                          Long expiresInSeconds) {
        long expiresAt = System.currentTimeMillis() + expiresInSeconds * 1000L;

        SnsToken token = snsTokenRepositoryPort.findByUserIdAndSnsType(userId, SnsType.youtube)
                .map(existing -> {
                    existing = SnsToken.builder()
                            .id(existing.getId())
                            .userId(userId)
                            .storeId(storeId)
                            .snsType(SnsType.youtube)
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .expiresAt(expiresAt)
                            .build();
                    return existing;
                })
                .orElse(SnsToken.builder()
                        .userId(userId)
                        .storeId(storeId)
                        .snsType(SnsType.youtube)
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .expiresAt(expiresAt)
                        .build());

        snsTokenRepositoryPort.save(token);
    }

    public SnsToken getTokenOrThrow(Long userId, Long storeId) {
        return snsTokenRepositoryPort.findByUserIdAndStoreIdAndSnsType(userId, storeId, SnsType.youtube)
                .orElseThrow(() -> new IllegalStateException("연동된 Youtube 토큰이 없습니다."));
    }
}
