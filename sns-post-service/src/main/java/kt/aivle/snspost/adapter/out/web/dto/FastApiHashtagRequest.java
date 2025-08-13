package kt.aivle.snspost.adapter.out.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastApiHashtagRequest {

    @JsonProperty("post_title")
    private String postTitle;

    @JsonProperty("post_content")
    private String postContent;

    @JsonProperty("user_keywords")
    private List<String> userKeywords;

    @JsonProperty("sns_platform")
    private String snsPlatform;

    @JsonProperty("business_type")
    private String businessType;

    private String location;
} 