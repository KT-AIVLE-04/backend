package kt.aivle.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Content extends BaseEntity {

    private Long userId;
    private Long storeId;

    private String title;
    private String originalName;

    @Column(length = 512)
    private String objectKey;

    private String contentType;

    private Integer width;
    private Integer height;
    private Integer durationSeconds;

    private Long bytes;

    @Builder
    public Content(Long userId, Long storeId, String title, String originalName, String objectKey,
                   String contentType, Integer width, Integer height, Integer durationSeconds, Long bytes) {
        this.userId = userId;
        this.storeId = storeId;
        this.title = title;
        this.originalName = originalName;
        this.objectKey = objectKey;
        this.contentType = contentType;
        this.width = width;
        this.height = height;
        this.durationSeconds = durationSeconds;
        this.bytes = bytes;
    }

    public void updateTitle(String title) {
        this.title = title;
    }
}