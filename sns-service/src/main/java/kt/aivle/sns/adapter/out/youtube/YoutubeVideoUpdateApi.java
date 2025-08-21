package kt.aivle.sns.adapter.out.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class YoutubeVideoUpdateApi {

    private final YoutubeClientFactory youtubeClientFactory;

    public void updateVideo(Long userId,
                            Long storeId,
                            String snsPostId,
                            String title,
                            String description,
                            String[] tags) {
        try {
            YouTube youtube = youtubeClientFactory.youtube(userId, storeId);

            Video videoMetadata = new Video();
            videoMetadata.setId(snsPostId);

            VideoStatus status = new VideoStatus();
            videoMetadata.setStatus(status);

            VideoSnippet snippet = new VideoSnippet();
            if (title != null) snippet.setTitle(title);
            if (description != null) snippet.setDescription(description);
            if (tags != null) snippet.setTags(Arrays.asList(tags));
            videoMetadata.setSnippet(snippet);

            // 부분 업데이트는 "snippet,status"만으로 충분
            youtube.videos().update("snippet,status", videoMetadata).execute();

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("YouTube 업데이트 실패", e);
        }
    }
}