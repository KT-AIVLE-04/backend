package kt.aivle.sns.adapter.in.web.dto.response;

import kt.aivle.sns.domain.model.PostEntity;
import kt.aivle.sns.domain.model.SnsType;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Builder
public record PostResponse(
        Long id,
        String snsPostId,
        String title,
        String description,
        SnsType snsType,
        String originalName,
        String objectKey,
        String url,
        List<String> tags,
        String categoryId,
        OffsetDateTime publishAt,
        Boolean notifySubscribers
) {
    public static PostResponse from(PostEntity post) {
        return PostResponse.builder()
                .id(post.getId())
                .snsType(post.getSnsType())
                .snsPostId(post.getSnsPostId())
                .title(post.getTitle())
                .description(post.getDescription())
                .originalName(post.getOriginalName())
                .objectKey(post.getObjectKey())
                .tags(post.getTags() != null ? new ArrayList<>(post.getTags()) : Collections.emptyList())
                .categoryId(post.getCategoryId())
                .publishAt(post.getPublishAt())
                .notifySubscribers(post.getNotifySubscribers())
                .build();
    }

    public static PostResponse from(PostEntity post, String url) {
        return PostResponse.builder()
                .id(post.getId())
                .snsType(post.getSnsType())
                .snsPostId(post.getSnsPostId())
                .title(post.getTitle())
                .description(post.getDescription())
                .url(url)
                .originalName(post.getOriginalName())
                .objectKey(post.getObjectKey())
                .tags(post.getTags() != null ? new ArrayList<>(post.getTags()) : Collections.emptyList())
                .categoryId(post.getCategoryId())
                .publishAt(post.getPublishAt())
                .notifySubscribers(post.getNotifySubscribers())
                .build();
    }
}
