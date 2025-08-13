package kt.aivle.sns.adapter.out.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import kt.aivle.sns.application.service.youtube.YoutubeTokenService;
import kt.aivle.sns.config.YoutubeOAuthProperties;
import kt.aivle.sns.domain.model.SnsToken;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class YoutubeCredentialProvider {

    private final YoutubeTokenService tokenService;
    private final YoutubeOAuthProperties properties;

    public Credential getCredential(Long userId, Long storeId) throws IOException, GeneralSecurityException {

        SnsToken token = tokenService.getTokenOrThrow(userId, storeId);

        if (token.getExpiresAt() != null && token.getExpiresAt() <= System.currentTimeMillis() + 60_000) {
            token = refresh(userId, storeId, token);
        }

        return new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(properties.getClientId(), properties.getClientSecret())
                .build()
                .setAccessToken(token.getAccessToken())
                .setRefreshToken(token.getRefreshToken())
                .setExpirationTimeMilliseconds(token.getExpiresAt());
    }

    private SnsToken refresh(Long userId, Long storeId, SnsToken token) {
        if (token.getRefreshToken() == null || token.getRefreshToken().isBlank()) {
            throw new IllegalStateException("Refresh token이 없어 access token을 갱신할 수 없습니다.");
        }
        try {
            var resp = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance(),
                    properties.getClientId(), properties.getClientSecret(),
                    token.getRefreshToken(), ""          // refresh grant에는 redirect 불필요
            ).setGrantType("refresh_token").execute();

            var newAccess = resp.getAccessToken();
            var expiresIn = resp.getExpiresInSeconds() == null ? 3600 : resp.getExpiresInSeconds();
            tokenService.saveToken(userId, storeId, newAccess, null, expiresIn);
            return tokenService.getTokenOrThrow(userId, storeId);
        } catch (Exception e) {
            throw new RuntimeException("YouTube access token 갱신 실패", e);
        }
    }
}
