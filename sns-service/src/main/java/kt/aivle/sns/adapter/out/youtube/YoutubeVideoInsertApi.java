package kt.aivle.sns.adapter.out.youtube;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.sns.infra.S3Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Locale;

import static kt.aivle.sns.exception.SnsErrorCode.FAIL_UPLOAD;

@Component
@RequiredArgsConstructor
@Slf4j
public class YoutubeVideoInsertApi {

    private final YoutubeClientFactory youtubeClientFactory;
    private final S3Storage s3Storage; // S3 스트리밍

    /** 업로드 성공 시 videoId 반환 */
    public String uploadVideo(Long userId,
                              Long storeId,
                              String objectKey,          // S3 Object Key (예: videos/2025/08/xxx.mp4)
                              String title,
                              String description,
                              String[] tags,
                              String categoryId,
                              boolean notifySubscribers,
                              OffsetDateTime publishAt) {
        try {
            YouTube youtube = youtubeClientFactory.youtube(userId, storeId);

            // 메타데이터
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("private"); // 예약 게시하려면 private + publishAt
            if (publishAt != null) {
                status.setPublishAt(new DateTime(publishAt.toInstant().toEpochMilli()));
            }
            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(title);
            snippet.setDescription(description);
            if (tags != null) snippet.setTags(Arrays.asList(tags));
            if (categoryId != null) snippet.setCategoryId(categoryId);

            Video videoMetadata = new Video();
            videoMetadata.setStatus(status);
            videoMetadata.setSnippet(snippet);

            // S3에서 스트리밍 + Content-Length/MIME 결정
            try (var s3obj = s3Storage.fetchForUpload(objectKey)) {
                String s3Ct = s3obj.contentType();
                String mediaType = resolveVideoContentType(s3Ct, objectKey);

                log.info("[YouTubeUpload] key={}, s3ContentType={}, useContentType={}, length={}",
                        objectKey, s3Ct, mediaType, s3obj.contentLength());

                InputStreamContent mediaContent = new InputStreamContent(mediaType, s3obj.stream());
                mediaContent.setLength(s3obj.contentLength());

                YouTube.Videos.Insert insert = youtube.videos()
                        .insert("snippet,status", videoMetadata, mediaContent);
                insert.setNotifySubscribers(notifySubscribers);

                MediaHttpUploader uploader = insert.getMediaHttpUploader();
                uploader.setDirectUploadEnabled(false);        // Resumable
                uploader.setChunkSize(10 * 1024 * 1024);       // 10MB

                return insert.execute().getId();
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new BusinessException(FAIL_UPLOAD, e.getMessage());
        }
    }

    /** S3 Content-Type가 부정확할 때, 확장자로 안전한 비디오 MIME을 결정 */
    private String resolveVideoContentType(String s3ContentType, String key) {
        if (s3ContentType != null) {
            String ct = s3ContentType.toLowerCase(Locale.ROOT).trim();
            if (!ct.equals("application/octet-stream") && !ct.equals("binary/octet-stream") && !ct.isBlank()) {
                return ct;
            }
        }
        String lower = key.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".mp4"))  return "video/mp4";
        if (lower.endsWith(".m4v"))  return "video/x-m4v";
        if (lower.endsWith(".mov"))  return "video/quicktime";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".mkv"))  return "video/x-matroska";
        if (lower.endsWith(".mpeg") || lower.endsWith(".mpg")) return "video/mpeg";
        // 알 수 없으면 mp4로 가정(YouTube 호환성 가장 높음)
        return "video/mp4";
    }
}