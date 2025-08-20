package kt.aivle.sns.adapter.in.web.dto;

import kt.aivle.sns.domain.model.PostEntity;
import kt.aivle.sns.domain.model.SnsAccount;
import kt.aivle.sns.domain.model.SnsType;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record PostUploadResponse(
        Long id,
        Long userId,
        SnsType snsType,
        String postId,
        String title,
        String description,
        String contentPath,
        List<String> tags,
        String categoryId,
        OffsetDateTime publishAt,
        Boolean notifySubscribers
) {
    public static PostUploadResponse from(PostEntity post) {
        return PostUploadResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .snsType(post.getSnsType())
                .postId(post.getPostId())
                .title(post.getTitle())
                .description(post.getDescription())
                .contentPath(post.getContentPath())
                .tags(post.getTags())
                .categoryId(post.getCategoryId())
                .publishAt(post.getPublishAt())
                .notifySubscribers(post.getNotifySubscribers())
                .build();
    }
}
