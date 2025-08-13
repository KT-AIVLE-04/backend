package kt.aivle.analytics.adapter.in.event.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialPostResponseEvent {
    
    private String requestId;
    private String userId;
    private String snsType;
    private List<SocialPost> posts;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialPost {
        private Long id;
        private Long socialAccountId;
        private Long postId;
        private String snsPostId; // YouTube video ID, Instagram post ID 등
        private String status;
        private LocalDateTime postedAt;
        private String title;        // YouTube 비디오 제목
        private String description;  // YouTube 비디오 설명
        private String thumbnailUrl; // YouTube 썸네일 URL
    }
}
