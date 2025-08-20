package kt.aivle.sns.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private SnsType snsType;

    private String postId; // 유튜브 videoId

    private String title;

    private String description;

    private String contentPath; // S3경로

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags; // (유튜브에선 사용자에게만 보임 알고리즘을 위한 태그)
    // tags를 별도 테이블로 분리하여 @ElementCollection으로 저장 (단순 문자열 리스트일 경우 유용)

    private String categoryId;

    private OffsetDateTime publishAt;

    private Boolean notifySubscribers;

}
