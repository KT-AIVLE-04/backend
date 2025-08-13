package kt.aivle.sns.adapter.out.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;

import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class YoutubeClientFactory {

    private final YoutubeCredentialProvider credentialProvider;

    public YouTube youtube(Long userId, Long storeId) throws IOException, GeneralSecurityException {
        // SnsTokenStore에서 OAuth 인증 완료된 Credential을 가져옵니다.
        var credential = credentialProvider.getCredential(userId, storeId);

        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("sns-video-service").build();
    }

    public YouTubeAnalytics analytics(Long userId, Long storeId) throws IOException, GeneralSecurityException {
        var credential = credentialProvider.getCredential(userId, storeId);

        return new YouTubeAnalytics.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("sns-analytics-service").build();
    }
}
