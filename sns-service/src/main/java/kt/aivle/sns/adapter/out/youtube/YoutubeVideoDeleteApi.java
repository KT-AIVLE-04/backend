package kt.aivle.sns.adapter.out.youtube;

import com.google.api.services.youtube.YouTube;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class YoutubeVideoDeleteApi {

    private final YoutubeClientFactory youtubeClientFactory;

    public void deleteVideo(Long userId, Long storeId, String snsPostId) {
        try {
            YouTube youTube = youtubeClientFactory.youtube(userId, storeId);
            youTube.videos().delete(snsPostId).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("YouTube 삭제 실패", e);
        }
    }
}