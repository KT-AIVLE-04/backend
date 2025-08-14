package kt.aivle.content.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.content.dto.ContentMapper;
import kt.aivle.content.dto.ContentResponse;
import kt.aivle.content.dto.CreateContentRequest;
import kt.aivle.content.entity.Content;
import kt.aivle.content.infra.S3Storage;
import kt.aivle.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

import static kt.aivle.content.exception.ContentErrorCode.IMAGE_UPLOAD_ERROR;

@Service
@Transactional
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final ContentMapper contentMapper;
    private final S3Storage s3Storage;

    private static final String ORIGIN_KEY_PREFIX = "origin/";
    private static final String THUMB_KEY_PREFIX = "thumbnail/";

    @Override
    public ContentResponse uploadContent(CreateContentRequest request) {
        try {
            MultipartFile file = request.file();
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();

            String originKey = getOriginKey(uuid, originalFilename);
            String thumbKey = getThumbKey(uuid);

            // 1) 원본 업로드
            s3Storage.put(originKey, file, null);

            // 2) 메타 + 썸네일
            Integer width = null, height = null, durationSec = null;
            File thumbFile;

            if (contentType.startsWith("image")) {
                try (InputStream in = file.getInputStream()) {
                    var meta = MediaMetadataExtractor.extractImageMeta(in);
                    width = meta.width();
                    height = meta.height();
                }
                try (InputStream in = file.getInputStream()) {
                    thumbFile = ThumbnailGenerator.createImageThumbnail(in, 300, 300);
                }
            } else if (contentType.startsWith("video")) {
                File tmpVideo = File.createTempFile("upload-", getExtSafe(originalFilename, ".mp4"));
                file.transferTo(tmpVideo);

                // 메타
                var meta = MediaMetadataExtractor.extractVideoMeta(tmpVideo);
                width = meta.width();
                height = meta.height();
                durationSec = meta.durationSeconds();

                thumbFile = ThumbnailGenerator.createVideoThumbnail(tmpVideo, 300, 300);
            } else {
                throw new BusinessException(IMAGE_UPLOAD_ERROR, "지원하지 않는 파일 형식입니다: " + contentType);
            }

            try (InputStream tin = new FileInputStream(thumbFile)) {
                s3Storage.put(thumbKey, tin, thumbFile.length(), "image/jpeg", /*cacheControl*/ null);
            } finally {
                if (thumbFile != null) thumbFile.delete();
            }

            Content content = Content.builder()
                    .userId(request.userId())
                    .storeId(request.storeId())
                    .objectKey(uuid)
                    .title(originalFilename)
                    .originalName(originalFilename)
                    .contentType(contentType)
                    .width(width)
                    .height(height)
                    .durationSeconds(durationSec)
                    .bytes(file.getSize())
                    .build();

            Content saved = contentRepository.save(content);
            return contentMapper.toContentResponse(saved);
        } catch (Exception e) {
            throw new BusinessException(IMAGE_UPLOAD_ERROR, e.getMessage());
        }
    }

    private String getExtSafe(String name, String defExt) {
        if (name == null) return defExt;
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i) : defExt;
    }

    public static String getOriginKey(String uuid, String originalFilename) {
        return "origin/" + uuid + "-" + originalFilename;
    }

    public static String getThumbKey(String uuid) {
        return "thumbnail/" + uuid + ".jpg";
    }
}