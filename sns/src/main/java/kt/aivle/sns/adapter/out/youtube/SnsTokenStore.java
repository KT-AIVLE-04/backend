package kt.aivle.sns.adapter.out.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import kt.aivle.sns.adapter.out.persistence.SnsTokenRepository;
import kt.aivle.sns.config.YoutubeOAuthProperties;
import kt.aivle.sns.domain.model.SnsToken;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SnsTokenStore {
    private final SnsTokenRepository tokenRepository;

    private final YoutubeOAuthProperties properties;

    public void saveToken(String userId,
                          String accessToken,
                          String refreshToken,
                          Long expiresInSeconds) {
        long expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000);

        SnsToken token = tokenRepository.findByUserId(userId)
                .map(existing -> {
                    existing = SnsToken.builder()
                            .id(existing.getId())
                            .userId(userId)
                            .snsType(SnsType.youtube)
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .expiresAt(expiresAt)
                            .build();
                    return existing;
                })
                .orElse(SnsToken.builder()
                        .userId(userId)
                        .snsType(SnsType.youtube)
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .expiresAt(expiresAt)
                        .build());

        tokenRepository.save(token);
    }

    public String getAccessToken(String userId) {
        return tokenRepository.findByUserId(userId)
                .map(SnsToken::getAccessToken)
                .orElse(null);
    }

    public String getRefreshToken(String userId) {
        return tokenRepository.findByUserId(userId)
                .map(SnsToken::getRefreshToken)
                .orElse(null);
    }

    public Credential getCredential(String userId, SnsType snsType) throws IOException, GeneralSecurityException {
        if(snsType != SnsType.youtube) {
            throw new UnsupportedOperationException("현재는 YouTube만 지원합니다.");
        }

        // DB에서 access token, refresh token 등 조회
        SnsToken token = tokenRepository.findByUserIdAndSnsType(userId, snsType)
                .orElseThrow(() -> new IllegalStateException("토큰이 없습니다."));

        return new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(properties.getClientId(), properties.getClientSecret())
                .build()
                .setAccessToken(token.getAccessToken())
                .setRefreshToken(token.getRefreshToken())
                .setExpirationTimeMilliseconds(token.getExpiresAt());
    }
}
