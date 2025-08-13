package kt.aivle.snspost.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kt.aivle.snspost.domain.model.ContentType;
import kt.aivle.snspost.domain.model.SnsPlatform;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeneratePostRequest {

    @NotBlank(message = "콘텐츠 데이터는 필수입니다")
    private String contentData;

    @NotNull(message = "콘텐츠 타입은 필수입니다")
    private ContentType contentType = ContentType.IMAGE;

    @NotEmpty(message = "사용자 키워드는 필수입니다")
    private List<String> userKeywords;

    @NotNull(message = "SNS 플랫폼은 필수입니다")
    private SnsPlatform snsPlatform;

    @NotBlank(message = "업종은 필수입니다")
    private String businessType;

    private String location;
} 