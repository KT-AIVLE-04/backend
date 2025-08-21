// application/service/youtube/YoutubeSnsPostService.java
package kt.aivle.sns.application.service.youtube;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.sns.adapter.in.web.dto.request.PostCreateRequest;
import kt.aivle.sns.adapter.in.web.dto.request.PostDeleteRequest;
import kt.aivle.sns.adapter.in.web.dto.request.PostUpdateRequest;
import kt.aivle.sns.adapter.in.web.dto.response.PostResponse;
import kt.aivle.sns.adapter.out.youtube.YoutubeVideoDeleteApi;
import kt.aivle.sns.adapter.out.youtube.YoutubeVideoInsertApi;
import kt.aivle.sns.adapter.out.youtube.YoutubeVideoUpdateApi;
import kt.aivle.sns.application.event.PostEvent;
import kt.aivle.sns.application.port.in.SnsPostUseCase;
import kt.aivle.sns.application.port.out.EventPublisherPort;
import kt.aivle.sns.application.port.out.PostRepositoryPort;
import kt.aivle.sns.application.port.out.SnsAccountRepositoryPort;
import kt.aivle.sns.domain.model.PostEntity;
import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.infra.CloudFrontSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static kt.aivle.sns.exception.SnsErrorCode.NOT_FOUND_POST;

@Service
@Transactional
@RequiredArgsConstructor
public class YoutubeSnsPostService implements SnsPostUseCase {

    private final YoutubeVideoInsertApi youtubeVideoInsertApi;
    private final YoutubeVideoUpdateApi youtubeVideoUpdateApi;
    private final YoutubeVideoDeleteApi youtubeVideoDeleteApi;

    private final EventPublisherPort publisher;
    private final SnsAccountRepositoryPort snsAccountRepositoryPort;
    private final PostRepositoryPort postRepositoryPort;

    private final CloudFrontSigner cloudFrontSigner;

    private static final ZoneOffset KST = ZoneOffset.of("+09:00");

    @Override
    public SnsType supportSnsType() {
        return SnsType.youtube;
    }

    /**
     * 업로드(즉시/예약)
     */
    @Override
    public PostResponse upload(Long userId, Long storeId, PostCreateRequest request) {
        // 1) 입력 정리
        String originKey = getOriginKey(request.objectKey(), request.originalName());
        String title = request.title();
        String description = request.description();
        String[] tags = request.tags();
        String categoryId = null;                         // 요청에 없으니 당장은 null
        boolean notifySubscribers = true;                 // 기본값(필요하면 req에 추가)
        OffsetDateTime publishAt = resolvePublishAt(request.isNow(), request.publishAt());

        // 2) YouTube 업로드
        String videoId = youtubeVideoInsertApi.uploadVideo(
                userId, storeId, originKey, title, description, tags,
                categoryId, notifySubscribers, publishAt
        );

        // 3) 로컬 DB 저장
        List<String> tagList = (tags == null) ? List.of() : Arrays.asList(tags);
        PostEntity entity = PostEntity.builder()
                .userId(userId)
                .storeId(storeId)
                .snsType(SnsType.youtube)
                .snsPostId(videoId)
                .title(title)
                .description(description)
                .originalFileName(request.originalName()) // 요청에 있는 원본 파일명
                .objectKey(request.objectKey())         // 엔티티 필드명: ObjectKey
                .tags(tagList)
                .categoryId(categoryId)
                .publishAt(publishAt)
                .notifySubscribers(notifySubscribers)
                .build();
        PostEntity saved = postRepositoryPort.save(entity);

        // 4) 이벤트 발행
        SnsAccount account = snsAccountRepositoryPort
                .findByUserIdAndStoreIdAndSnsType(userId, storeId, SnsType.youtube)
                .orElseThrow();
        publisher.publishPostCreated(PostEvent.builder()
                .id(saved.getId())                 // 로컬 PK
                .accountId(account.getId())        // SNS 계정 PK
                .snsPostId(saved.getSnsPostId())   // YouTube videoId
                .publishAt(saved.getPublishAt())
                .build());

        return PostResponse.from(saved);
    }

    @Override
    public PostResponse update(Long userId, Long storeId, Long postId, PostUpdateRequest request) {
        PostEntity post = postRepositoryPort.findById(postId).orElseThrow(() -> new BusinessException(NOT_FOUND_POST));
        String snsPostId = post.getSnsPostId();

        // 1) YouTube 업데이트
        youtubeVideoUpdateApi.updateVideo(
                userId,
                storeId,
                snsPostId,
                request.title(),
                request.description(),
                request.tags()
        );

        // 2) 로컬 DB 반영
        if (request.title() != null) post.setTitle(request.title());
        if (request.description() != null) post.setDescription(request.description());
        if (request.tags() != null) post.setTags(Arrays.asList(request.tags()));

        // 예약 시각 변경(선택)
        if (request.publishAt() != null) {
            OffsetDateTime publishAt = resolvePublishAt(Boolean.TRUE.equals(request.isNow()), request.publishAt());
            post.setPublishAt(publishAt);
        }

        PostEntity saved = postRepositoryPort.save(post);
        return PostResponse.from(saved);
    }

    /**
     * 삭제
     */
    @Override
    public void delete(Long userId, Long storeId, Long postId, PostDeleteRequest request) {

        PostEntity post = postRepositoryPort.findById(postId).orElseThrow(() -> new BusinessException(NOT_FOUND_POST));
        String snsPostId = post.getSnsPostId();

        // 1) YouTube 삭제
        youtubeVideoDeleteApi.deleteVideo(userId, storeId, snsPostId);


        SnsAccount account = snsAccountRepositoryPort
                .findByUserIdAndStoreIdAndSnsType(userId, storeId, SnsType.youtube)
                .orElseThrow();

        postRepositoryPort.delete(post);

        publisher.publishPostDeleted(PostEvent.builder()
                .id(post.getId())
                .accountId(account.getId())
                .snsPostId(post.getSnsPostId())
                .build());
    }

    @Transactional(readOnly = true)
    @Override
    public PostResponse get(Long userId, Long storeId, Long postId) {
        PostEntity post = postRepositoryPort.findById(postId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_POST));

        String originKey = getOriginKey(post.getObjectKey(), post.getOriginalName());
        String signUrl = cloudFrontSigner.signOriginalUrl(originKey);

        return PostResponse.from(post, signUrl);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PostResponse> getAll(Long userId, Long storeId) {
        List<PostEntity> posts = postRepositoryPort.findAllByUserIdAndStoreId(userId, storeId);
        if (posts.isEmpty()) return List.of();

        return posts.stream()
                .map(post -> {
                    String objectKey = post.getObjectKey();
                    String thumbKey = getThumbKey(userId, storeId, objectKey);
                    String thumbUrl = cloudFrontSigner.getThumbUrl(thumbKey);
                    return PostResponse.from(post, thumbUrl);
                })
                .toList();
    }

    private OffsetDateTime resolvePublishAt(boolean isNow, LocalDateTime publishAt) {
        if (isNow) return OffsetDateTime.now(KST);
        if (publishAt == null) return null; // 예약이 아니면 null 허용
        return publishAt.atOffset(KST);
    }

    private String getOriginKey(String uuid, String originalFilename) {
        return "origin/" + uuid + "-" + originalFilename;
    }

    private String getThumbKey(long userId, long storeId, String uuid) {
        return "thumbnail/%d-%d/%s.jpg".formatted(userId, storeId, uuid);
    }
}
