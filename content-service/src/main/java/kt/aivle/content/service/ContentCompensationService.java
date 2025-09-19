package kt.aivle.content.service;

import kt.aivle.content.entity.Content;
import kt.aivle.content.infra.S3Storage;
import kt.aivle.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentCompensationService {

    private final ContentRepository contentRepository;
    private final S3Storage s3Storage;

    private static final String MEDIA_PREFIX = "media/";
    private static final String ORIGIN_DIR = "origin/";
    private static final String THUMB_DIR = "thumbnail/";

    @Transactional
    public void compensateContentCreation(Long userId, Long storeId, String sagaId) {
        log.info("Starting content compensation for userId: {}, storeId: {}, sagaId: {}",
                userId, storeId, sagaId);

        try {
            // sagaId와 연관된 콘텐츠를 찾아서 삭제
            // 실제로는 Content 엔티티에 sagaId 필드를 추가하거나,
            // 별도 매핑 테이블을 통해 sagaId와 contentId를 연결해야 함

            // 임시 방편: 최근 생성된 콘텐츠 중에서 해당 userId, storeId의 콘텐츠 삭제
            List<Content> recentContents = contentRepository
                    .findTop1ByUserIdAndStoreIdOrderByCreatedAtDesc(userId, storeId);

            for (Content content : recentContents) {
                deleteContentAndFiles(content);
                log.info("Compensated content deletion: contentId={}", content.getId());
            }

        } catch (Exception e) {
            log.error("Failed to compensate content creation for sagaId: {}, error: {}",
                    sagaId, e.getMessage(), e);
            throw e;
        }
    }

    private void deleteContentAndFiles(Content content) {
        try {
            // S3에서 파일 삭제
            String originKey = getOriginKey(content.getObjectKey(), content.getOriginalName());
            String thumbKey = getThumbKey(content.getUserId(), content.getStoreId(), content.getObjectKey());

            s3Storage.delete(originKey);
            s3Storage.delete(thumbKey);

            // DB에서 콘텐츠 삭제
            contentRepository.delete(content);

            log.info("Successfully deleted content and files: contentId={}, objectKey={}",
                    content.getId(), content.getObjectKey());

        } catch (Exception e) {
            log.error("Failed to delete content and files: contentId={}, error: {}",
                    content.getId(), e.getMessage(), e);
            throw e;
        }
    }

    private String getOriginKey(String uuid, String originalFilename) {
        return MEDIA_PREFIX + ORIGIN_DIR + uuid + "-" + originalFilename;
    }

    private String getThumbKey(long userId, long storeId, String uuid) {
        return MEDIA_PREFIX + THUMB_DIR + "%d-%d/%s.jpg".formatted(userId, storeId, uuid);
    }
}