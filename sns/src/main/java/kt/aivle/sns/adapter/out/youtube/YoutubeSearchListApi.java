package kt.aivle.sns.adapter.out.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class YoutubeSearchListApi {

    private final YoutubeClientFactory youtubeClientFactory;

    public void getYoutubeMyVideoList(Long userId) {

        try {
            // userId 기반으로 인증된 YouTube 객체 생성
            YouTube youtube = youtubeClientFactory.youtube(userId);

            YouTube.Search.List request = youtube.search()
                    .list("id,snippet");
            SearchListResponse response = request.setForMine(true)
                    .setMaxResults(25L)
                    .setType("video")
                    .execute();
            System.out.println(response);

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
