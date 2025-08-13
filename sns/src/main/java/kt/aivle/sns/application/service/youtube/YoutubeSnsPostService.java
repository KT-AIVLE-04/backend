package kt.aivle.sns.application.service.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt.aivle.sns.adapter.in.web.dto.*;
import kt.aivle.sns.adapter.out.youtube.YoutubeVideoDeleteApi;
import kt.aivle.sns.adapter.out.youtube.YoutubeVideoInsertApi;
import kt.aivle.sns.adapter.out.youtube.YoutubeVideoUpdateApi;
import kt.aivle.sns.application.port.in.SnsPostUseCase;
import kt.aivle.sns.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YoutubeSnsPostService implements SnsPostUseCase {

    private final YoutubeVideoInsertApi youtubeVideoInsertApi;
    private final YoutubeVideoUpdateApi youtubeVideoUpdateApi;
    private final YoutubeVideoDeleteApi youtubeVideoDeleteApi;

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
            youtubeVideoInsertApi.uploadVideo(
                    userId,
                    request.getContentPath(),
                    request.getTitle(),
                    request.getDescription(),
                    request.getTags(),
                    detail.getCategoryId(),
                    detail.isNotifySubscribers(),
                    detail.getPublishAt()
            );
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
                    request.getPostId()
            );
        } catch (Exception e) {
            throw new RuntimeException("YouTube 딜리트 실패", e);
        }
    }
}
