package kt.aivle.sns.application.service.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt.aivle.sns.adapter.in.web.dto.*;
import kt.aivle.sns.adapter.out.youtube.YoutubeVideoDeleteApi;
import kt.aivle.sns.adapter.out.youtube.YoutubeVideoInsertApi;
import kt.aivle.sns.adapter.out.youtube.YoutubeVideoUpdateApi;
import kt.aivle.sns.application.event.PostEvent;
import kt.aivle.sns.application.port.in.SnsPostUseCase;
import kt.aivle.sns.application.port.out.EventPublisherPort;
import kt.aivle.sns.application.port.out.PostRepositoryPort;
import kt.aivle.sns.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.sns.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class YoutubeSnsPostService implements SnsPostUseCase {

    private final YoutubeVideoInsertApi youtubeVideoInsertApi;
    private final YoutubeVideoUpdateApi youtubeVideoUpdateApi;
    private final YoutubeVideoDeleteApi youtubeVideoDeleteApi;
    private final EventPublisherPort publisher;

    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final PostRepositoryPort postRepositoryPort;


    private final ObjectMapper objectMapper;

    @Override
    public SnsType supportSnsType()  {
        return SnsType.youtube;
    }

    @Override
    public void upload(Long userId, PostUploadRequest request) {

//        System.out.println("ObjectMapper: " + objectMapper);
        YoutubeUploadDetail detail = objectMapper.convertValue(request.getDetail(), YoutubeUploadDetail.class);

        try {
            PostUploadResponse response =  youtubeVideoInsertApi.uploadVideo(
                    userId,
                    request.getStoreId(),
                    request.getContentPath(),
                    request.getTitle(),
                    request.getDescription(),
                    request.getTags(),
                    detail.getCategoryId(),
                    detail.isNotifySubscribers(),
                    detail.getPublishAt()
            );

            if (response == null) return;
            SnsAccount account = snsAccountRepositoryPort.findByUserIdAndStoreIdAndSnsType(userId, request.getStoreId(), SnsType.youtube)
                    .orElseThrow();


            publisher.publishPostCreated(PostEvent.builder()
                    .id(response.id())                  // 내 게시글 PK
                    .accountId(account.getId())         // 내 SNS계정 PK
                    .snsPostId(response.postId())       // SNS측 게시글 ID
                    .build());

        } catch (Exception e) {
            throw new RuntimeException("YouTube 업로드 실패", e);
        }
    }

    @Override
    public void update(Long userId, PostUpdateRequest request) {

        YoutubeUpdateDetail detail = objectMapper.convertValue(request.getDetail(), YoutubeUpdateDetail.class);

        try {
            youtubeVideoUpdateApi.updateVideo(
                    userId,
                    request.getStoreId(),
                    request.getPostId(),
                    request.getTitle(),
                    request.getDescription(),
                    request.getTags(),
                    detail.getCategoryId()
            );
        } catch (Exception e) {
            throw new RuntimeException("YouTube 업데이트 실패", e);
        }
    }

    @Override
    public void delete(Long userId, PostDeleteRequest request) {

        try {
            youtubeVideoDeleteApi.deleteVideo(
                    userId,
                    request.getStoreId(),
                    request.getPostId()
            );

            SnsAccount account = snsAccountRepositoryPort.findByUserIdAndStoreIdAndSnsType(userId, request.getStoreId(), SnsType.youtube)
                    .orElseThrow();

            PostEntity post = postRepositoryPort.findByPostId(request.getPostId())
                    .orElseThrow();


            publisher.publishPostDeleted(PostEvent.builder()
                    .id(post.getId())                  // 내 게시글 PK
                    .accountId(account.getId())         // 내 SNS계정 PK
                    .snsPostId(post.getPostId())       // SNS측 게시글 ID
                    .build());

        } catch (Exception e) {
            throw new RuntimeException("YouTube 딜리트 실패", e);
        }
    }
}
