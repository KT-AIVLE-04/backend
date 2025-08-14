package kt.aivle.snspost.adapter.in.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SNSPostRequest {

    @JsonProperty("content_data")
    @NotBlank(message = "콘텐츠 데이터를 입력해주세요.")
    private String contentData;

    @JsonProperty("user_keywords")
    private List<String> userKeywords;

    @JsonProperty("sns_platform")
    @NotBlank(message = "SNS 플랫폼을 입력해주세요.")
    private String snsPlatform;

    @JsonProperty("business_type")
    @NotBlank(message = "업종을 입력해주세요.")
    private String businessType;

    private String location;
}
