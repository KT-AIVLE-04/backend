package kt.aivle.sns.adapter.out.youtube;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import kt.aivle.sns.adapter.out.persistence.JpaPostRepository;
import kt.aivle.sns.domain.model.PostEntity;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class YoutubeVideoInsertApi {

    private final YoutubeClientFactory youtubeClientFactory;

    private final JpaPostRepository jpaPostRepository;

    public void uploadVideo(Long userId,
                            Long storeId,
                            String contentPath,
                            String title,
                            String description,
                            String[] tags,
                            String categoryId,
                            boolean notifySubscribers,
                            OffsetDateTime publishAt) {
        try {
            // userId 기반으로 인증된 YouTube 객체 생성
            YouTube youtube = youtubeClientFactory.youtube(userId, storeId);

            // 1. 동영상 메타데이터 생성
            Video videoMetadata = new Video();

            // status 설정
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("private"); // 예약공개는 반드시 private
            if(publishAt != null) {
                status.setPublishAt(new DateTime(publishAt.toInstant().toEpochMilli()));
            }
            videoMetadata.setStatus(status);

            // snippet 설정
            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(title);
            snippet.setDescription(description);
            snippet.setTags(Arrays.asList(tags));
            snippet.setCategoryId(categoryId);
            videoMetadata.setSnippet(snippet);

            // 2. 파일 읽기 및 업로드 준비 (테스트 : contentPath가 로컬)
            InputStreamContent mediaContent = new InputStreamContent(
                    "video/*", new FileInputStream(contentPath));
            mediaContent.setLength(new java.io.File(contentPath).length());
            /* 2-1. contentPath가 S3 URL
            InputStream inputStream = new URL(contentPath).openStream(); // IOException 처리 필요
            InputStreamContent mediaContent = new InputStreamContent(
                    "video/*",
                    new BufferedInputStream(inputStream)
            );
             */


            // 3. 업로드 요청
            YouTube.Videos.Insert videoInsert = youtube.videos()
                    .insert("snippet,status", videoMetadata, mediaContent);
            videoInsert.setNotifySubscribers(notifySubscribers);

            // 4. 업로드 진행 상황 출력
            MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false); // resumable upload
            uploader.setProgressListener(new MediaHttpUploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case INITIATION_STARTED:
                            System.out.println("업로드 시작...");
                            break;
                        case MEDIA_IN_PROGRESS:
                            System.out.printf("업로드 중... %.2f%% 완료%n", uploader.getProgress() * 100);
                            break;
                        case MEDIA_COMPLETE:
                            System.out.println("업로드 완료!");
                            break;
                        default:
                            break;
                    }
                }
            });

            // 5. 업로드 실행
            Video uploadedVideo = videoInsert.execute();
            System.out.println("업로드된 비디오 ID: " + uploadedVideo.getId()); // youtube에 저장된 비디오 ID

            // 6. Post 저장
            String videoId = uploadedVideo.getId();
            PostEntity post = jpaPostRepository.findByPostId(videoId)
                    .map(existing -> {
                        existing = PostEntity.builder()
                                .id(existing.getId())
                                .userId(userId)
                                .snsType(SnsType.youtube)
                                .postId(videoId)
                                .title(title)
                                .description(description)
                                .contentPath(contentPath)
                                .tags(Arrays.asList(tags))
                                .categoryId(categoryId)
                                .notifySubscribers(notifySubscribers)
                                .publishAt(publishAt)
                                .build();
                        return existing;
                    })
                    .orElse(PostEntity.builder()
                            .userId(userId)
                            .snsType(SnsType.youtube)
                            .postId(videoId)
                            .title(title)
                            .description(description)
                            .contentPath(contentPath)
                            .tags(Arrays.asList(tags))
                            .categoryId(categoryId)
                            .notifySubscribers(notifySubscribers)
                            .publishAt(publishAt)
                            .build());
            jpaPostRepository.save(post);

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("YouTube 업로드 실패", e);
        }
    }
}
