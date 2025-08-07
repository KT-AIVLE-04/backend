package kt.aivle.sns.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnsAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;  // 사용자 id

    @Enumerated(EnumType.STRING)
    private SnsType snsType;    // sns 타입

    private String snsAccountId;   // 유튜브 채널 id

    private String snsAccountName;  // 채널명

    private String snsAccountDescription;   // 채널 설명

    private String snsAccountUrl;   // 사용자 채널 url ("https://www.youtube.com/" + url)

    private Integer follower;   // 구독자 수

    private Integer postCount;  // 업로드 동영상 수

    private Integer viewCount;  // 전체 동영상 조회수

    @ElementCollection
    @CollectionTable(name = "account_keywords", joinColumns = @JoinColumn(name = "sns_account_id"))
    @Column(name = "keyword")
    private List<String> keywords;  // 채널 키워드 (사용자에게만 보임 알고리즘을 위한 키워드)


}
