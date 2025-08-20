package kt.aivle.sns.adapter.out.youtube;

import com.google.api.services.youtube.YouTube;
import kt.aivle.sns.adapter.out.persistence.JpaPostRepository;
import kt.aivle.sns.application.port.out.PostRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class YoutubeVideoDeleteApi {

    private final YoutubeClientFactory youtubeClientFactory;

    private final PostRepositoryPort postRepositoryPort;

    public void deleteVideo(Long userId,
                            Long storeId,
                            String postId) {

        try {
            YouTube youTube = youtubeClientFactory.youtube(userId, storeId);

            // 삭제 요청 api 생성
            YouTube.Videos.Delete videoDelete = youTube.videos()
                    .delete(postId);

            // 삭제 실행
            videoDelete.execute();
            System.out.println("삭제됨");

            // 게시물(DB)Post 삭제
            postRepositoryPort.findByPostId(postId)
                    .ifPresent(postRepositoryPort::delete);



        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("YouTube 업데이트 실패", e);
        }
    }
}
