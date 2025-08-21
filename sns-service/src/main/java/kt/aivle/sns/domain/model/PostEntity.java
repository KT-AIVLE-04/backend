package kt.aivle.sns.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity extends BaseEntity {

    private Long userId;
    private Long storeId;

    @Enumerated(EnumType.STRING)
    private SnsType snsType;

    private String snsPostId; // 유튜브 videoId

    private String title;

    private String description;

    private String originalName; // 원본 파일 이름
    private String ObjectKey; // S3경로

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    private String categoryId;

    private OffsetDateTime publishAt;

    private Boolean notifySubscribers;

    @Builder
    public PostEntity(Long userId, Long storeId, SnsType snsType, String snsPostId, String title,
                      String description, String originalName, String objectKey, List<String> tags, String categoryId,
                      OffsetDateTime publishAt, Boolean notifySubscribers) {
        this.userId = userId;
        this.storeId = storeId;
        this.snsType = snsType;
        this.snsPostId = snsPostId;
        this.title = title;
        this.description = description;
        this.originalName = originalName;
        this.ObjectKey = objectKey;
        this.tags = tags;
        this.categoryId = categoryId;
        this.publishAt = publishAt;
        this.notifySubscribers = notifySubscribers;
    }
}
