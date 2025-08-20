package kt.aivle.sns.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEvent {
    private Long id;          // 내부 post PK
    private Long accountId;   // SNS 계정 PK (내 DB)
    private String snsPostId; // SNS 측 게시글 ID(생성 후 채워짐)
    private OffsetDateTime publishAt; // 사용자가 입력한 게시 시간
}
