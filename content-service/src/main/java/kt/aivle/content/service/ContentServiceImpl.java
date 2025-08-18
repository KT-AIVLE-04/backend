package kt.aivle.content.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.content.dto.ContentMapper;
import kt.aivle.content.dto.request.*;
import kt.aivle.content.dto.response.ContentDetailResponse;
import kt.aivle.content.dto.response.ContentResponse;
import kt.aivle.content.entity.Content;
import kt.aivle.content.event.CreateContentRequestMessage;
import kt.aivle.content.infra.CloudFrontSigner;
import kt.aivle.content.infra.S3Storage;
import kt.aivle.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static kt.aivle.content.exception.ContentErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final ContentMapper contentMapper;
    private final S3Storage s3Storage;
    private final CloudFrontSigner cloudFrontSigner;
    private final UrlDownloader urlDownloader;

    @Override
    public ContentResponse uploadContent(CreateContentRequest request) {
        try {
            MultipartFile file = request.file();
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();

            String originKey = getOriginKey(uuid, originalFilename);
            String thumbKey = getThumbKey(request.userId(), request.storeId(), uuid);

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

    @Transactional(readOnly = true)
    @Override
    public ContentDetailResponse getContentDetail(GetContentRequest request) {
        Content content = contentRepository.findById(request.id()).orElseThrow(() -> new BusinessException(NOT_FOUND_CONTENT));

        if (!isValidOwner(content, request.userId(), request.storeId())) {
            throw new BusinessException(NOT_AUTHORIZED_CONTENT);
        }

        String originKey = getOriginKey(content.getObjectKey(), content.getOriginalName());
        String signedUrl = cloudFrontSigner.signOriginalUrl(originKey);

        return contentMapper.toContentDetailResponse(content, signedUrl);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ContentResponse> getContents(GetContentListRequest request) {

        String q = request.query() == null ? null : request.query().trim();
        boolean noQuery = (q == null || q.isEmpty());

        List<Content> contents = noQuery
                ? contentRepository.findByUserIdAndStoreIdOrderByCreatedAtDesc(request.userId(), request.storeId())
                : contentRepository.findByUserIdAndStoreIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                request.userId(), request.storeId(), q
        );

        return contents.stream().map(content -> {
            String thumbKey = getThumbKey(request.userId(), request.storeId(), content.getObjectKey());
            String thumbUrl = cloudFrontSigner.getThumbUrl(thumbKey);
            return contentMapper.toContentResponse(content, thumbUrl);
        }).toList();
    }

    @Override
    public ContentDetailResponse updateContent(UpdateContentRequest request) {
        Content content = contentRepository.findById(request.id()).orElseThrow(() -> new BusinessException(NOT_FOUND_CONTENT));

        if (!isValidOwner(content, request.userId(), request.storeId())) {
            throw new BusinessException(NOT_AUTHORIZED_CONTENT);
        }

        content.updateTitle(request.title());

        Content updated = contentRepository.save(content);
        String originKey = getOriginKey(content.getObjectKey(), content.getOriginalName());
        String signedUrl = cloudFrontSigner.signOriginalUrl(originKey);

        return contentMapper.toContentDetailResponse(updated, signedUrl);
    }

    @Override
    public void deleteContent(DeleteContentRequest request) {
        Content content = contentRepository.findById(request.id()).orElseThrow(() -> new BusinessException(NOT_FOUND_CONTENT));

        if (!isValidOwner(content, request.userId(), request.storeId())) {
            throw new BusinessException(NOT_AUTHORIZED_CONTENT);
        }

        String originKey = getOriginKey(content.getObjectKey(), content.getOriginalName());
        String thumbKey = getThumbKey(request.userId(), request.storeId(), content.getObjectKey());

        s3Storage.delete(originKey);
        s3Storage.delete(thumbKey);

        contentRepository.delete(content);
    }

    @Override
    public void uploadContent(CreateContentRequestMessage request) {
        try {
            var dl = urlDownloader.fetch(request.url());

            String uuid = UUID.randomUUID().toString();
            String originKey = getOriginKey(uuid, dl.originalName());
            String thumbKey = getThumbKey(request.userId(), request.storeId(), uuid);

            Integer width = null, height = null, duration = null;
            File thumb = null;

            if (dl.contentType() != null && dl.contentType().startsWith("image")) {
                try (InputStream in = Files.newInputStream(dl.file())) {
                    var meta = MediaMetadataExtractor.extractImageMeta(in);
                    width = meta.width();
                    height = meta.height();
                }
                try (InputStream in = Files.newInputStream(dl.file())) {
                    thumb = ThumbnailGenerator.createImageThumbnail(in, 300, 300);
                }
            } else {
                File tmpVideo = dl.file().toFile();
                var meta = MediaMetadataExtractor.extractVideoMeta(tmpVideo);
                width = meta.width();
                height = meta.height();
                duration = meta.durationSeconds();
                thumb = ThumbnailGenerator.createVideoThumbnail(tmpVideo, 300, 300);
            }

            try (InputStream in = Files.newInputStream(dl.file())) {
                s3Storage.put(originKey, in, dl.size(), dl.contentType(), null);
            }

            if (thumb.exists()) {
                try (InputStream tin = new FileInputStream(thumb)) {
                    s3Storage.put(thumbKey, tin, thumb.length(), "image/jpeg", null);
                } finally {
                    try {
                        thumb.delete();
                    } catch (Exception ignore) {
                    }
                }
            }

            Content content = Content.builder()
                    .userId(request.userId())
                    .storeId(request.storeId())
                    .objectKey(uuid)
                    .title(dl.originalName())
                    .originalName(dl.originalName())
                    .contentType(dl.contentType())
                    .width(width)
                    .height(height)
                    .durationSeconds(duration)
                    .bytes(dl.size())
                    .build();
            contentRepository.save(content);

            try {
                Files.deleteIfExists(dl.file());
            } catch (Exception ignore) {
            }
        } catch (Exception e) {
            throw new BusinessException(URI_DOWNLOAD_ERROR, e.getMessage());
        }
    }

    private String getExtSafe(String name, String defExt) {
        if (name == null) return defExt;
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i) : defExt;
    }

    private String getOriginKey(String uuid, String originalFilename) {
        return "origin/" + uuid + "-" + originalFilename;
    }

    private String getThumbKey(long userId, long storeId, String uuid) {
        return "thumbnail/%d-%d/%s.jpg".formatted(userId, storeId, uuid);
    }

    private boolean isValidOwner(Content content, Long userId, Long storeId) {
        return Objects.equals(content.getUserId(), userId) && Objects.equals(content.getStoreId(), storeId);
    }
}