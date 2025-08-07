package kt.aivle.sns.adapter.out.youtube;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import kt.aivle.sns.application.port.out.PostRepository;
import kt.aivle.sns.config.YoutubeConfig;
import kt.aivle.sns.domain.model.PostEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class YoutubeVideoUpdateApi {

    private final YoutubeConfig youtubeConfig;

    private final PostRepository postRepository;

    public void updateVideo(String userId,
                            String postId,
                            String title,
                            String description,
                            String[] tags,
                            String categoryId) {

        try {
            // userId 기반으로 인증된 YouTube 객체 생성
            YouTube youtube = youtubeConfig.createYoutubeClient(userId);

            // 1. 동영상 메타데이터 생성
            Video videoMetadata = new Video();

            // 동영상 지정
            videoMetadata.setId(postId);

            // status 설정
            VideoStatus status = new VideoStatus();
            videoMetadata.setStatus(status);

            // snippet 설정
            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(title);
            snippet.setDescription(description);
            snippet.setTags(Arrays.asList(tags));
            snippet.setCategoryId(categoryId);
            videoMetadata.setSnippet(snippet);

            // 3. 업데이트 요청 api 생성
            YouTube.Videos.Update videoUpdate = youtube.videos()
                    .update("snippet,status,localizations", videoMetadata);

            // 4. 업로드 실행
            Video updatedVideo = videoUpdate.execute();
            System.out.println("업데이트된 비디오 ID: " + updatedVideo.getId()); // youtube에 저장된 비디오 ID

            // 5. 게시물(DB)Post 저장
            String videoId = updatedVideo.getId();
            Optional<PostEntity> optionalPost = postRepository.findByPostId(videoId);
            if(optionalPost.isPresent()) {
                PostEntity post = optionalPost.get();
                post.setTitle(title);
                post.setDescription(description);
                post.setTags(new ArrayList<>(Arrays.asList(tags)));
                post.setCategoryId(categoryId);

                postRepository.save(post);
            } else {
                System.err.println("해당 videoId를 가진 게시물을 찾을 수 없습니다: " + videoId);
            }



        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("YouTube 업데이트 실패", e);
        }
    }
}
