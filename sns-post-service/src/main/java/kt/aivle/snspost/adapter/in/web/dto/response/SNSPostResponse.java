package kt.aivle.snspost.adapter.in.web.dto.response;

import java.util.List;

public record SNSPostResponse(String title, String content, List<String> hashtags) {
}
