package kt.aivle.sns.application.service.youtube;

import jakarta.transaction.Transactional;
import kt.aivle.sns.application.port.in.TokenRefresher;
import kt.aivle.sns.application.port.in.TokenServiceUseCase;
import kt.aivle.sns.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.sns.application.port.out.SnsTokenRepositoryPort;
import kt.aivle.sns.config.YoutubeOAuthProperties;
import kt.aivle.sns.domain.model.RefreshedToken;
import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsToken;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YoutubeTokenService implements TokenServiceUseCase {
    private final SnsTokenRepositoryPort snsTokenRepositoryPort;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final YoutubeOAuthProperties props;

    private final List<TokenRefresher> refreshers;

    private static final long SKEW_MS = 60_000L;

    @Override
    public SnsType supportSnsType() {
        return SnsType.youtube;
    }

    @Override
    public void saveToken(Long userId,
                          Long storeId,
                          String accessToken,
                          String refreshToken,
                          Long expiresInSeconds) {
        long expiresAt = System.currentTimeMillis() + (expiresInSeconds != null ? expiresInSeconds : 3600) * 1000L;

        SnsToken token = snsTokenRepositoryPort.findByUserIdAndStoreIdAndSnsType(userId, storeId, SnsType.youtube)
                .orElseGet(() -> SnsToken.builder()
                        .userId(userId)
                        .storeId(storeId)
                        .snsType(SnsType.youtube)
                        .build());

        // 기존 엔티티를 새 builder로 갈아끼우지 말고 필드만 업데이트(Version/영속성 보호)
        token.setAccessToken(accessToken);
        if (refreshToken != null && !refreshToken.isBlank()) {
            token.setRefreshToken(refreshToken); // null/빈 응답이면 기존 값 유지
        }
        token.setExpiresAt(expiresAt);

        snsTokenRepositoryPort.save(token);
    }

    @Override
    public SnsToken getTokenOrThrow(Long userId, Long storeId) {
        return snsTokenRepositoryPort.findByUserIdAndStoreIdAndSnsType(userId, storeId, SnsType.youtube)
                .orElseThrow(() -> new IllegalStateException("연동된 Youtube 토큰이 없습니다."));
    }

    @Override
    public SnsToken getTokenByAccountOrThrow(String accountId) {
        SnsAccount account = snsAccountRepositoryPort.findBySnsAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        return getTokenOrThrow(account.getUserId(), account.getStoreId());
    }


    @Override
    @Transactional
    public SnsToken ensureValidToken(Long userId, Long storeId) {
        SnsToken token = getTokenOrThrow(userId, storeId);
        Long exp = token.getExpiresAt();

        if (exp != null && exp <= System.currentTimeMillis() + SKEW_MS) {
            return refresh(token);
        }
        return token;
    }

    @Override
    @Transactional
    public SnsToken refresh(SnsToken token) {
        String rt = token.getRefreshToken();
        if (rt == null || rt.isBlank()) {
            throw new IllegalStateException("refresh_token이 없어 갱신할 수 없습니다.");
        }

        TokenRefresher refresher = refreshers.stream()
                .filter(r -> r.supports(SnsType.youtube))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("YouTube refresher가 설정되지 않았습니다."));

        RefreshedToken nt = refresher.refresh(props.getClientId(), props.getClientSecret(), rt);

        token.setAccessToken(nt.getAccessToken());
        token.setExpiresAt(nt.getExpiresAt());
        // 구글이 종종 refresh_token을 응답하지 않으므로 null/빈 값이면 기존 유지
        if (nt.getRefreshToken() != null && !nt.getRefreshToken().isBlank()) {
            token.setRefreshToken(nt.getRefreshToken());
        }

        return snsTokenRepositoryPort.save(token);
    }
}
