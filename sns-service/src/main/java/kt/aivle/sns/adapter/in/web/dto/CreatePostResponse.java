package kt.aivle.sns.adapter.in.web.dto.response;

import java.util.List;

public record CreatePostResponse(String title, String content, List<String> hashtags) {
}
