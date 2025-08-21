package kt.aivle.sns.adapter.out.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import kt.aivle.sns.application.service.youtube.YoutubeTokenService;
import kt.aivle.sns.config.YoutubeOAuthProperties;
import kt.aivle.sns.domain.model.SnsToken;
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

        SnsToken token = tokenService.ensureValidToken(userId, storeId);

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(token.getAccessToken());
        tokenResponse.setExpiresInSeconds((token.getExpiresAt() - System.currentTimeMillis()) / 1000L); // 초 단위로 변환 필요
        tokenResponse.setRefreshToken(token.getRefreshToken());

        return new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(new GsonFactory())
                .setClientSecrets(properties.getClientId(), properties.getClientSecret())
                .build()
                .setFromTokenResponse(tokenResponse);
    }
}
