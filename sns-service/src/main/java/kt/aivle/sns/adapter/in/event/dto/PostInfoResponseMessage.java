package kt.aivle.sns.adapter.in.event.dto;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostInfoResponseMessage {
    private Long postId;
    private String title;
    private String description;
    private String url;
    private List<String> tags;
    private OffsetDateTime publishAt;
    private String snsPostId;
    private String snsType;
}
