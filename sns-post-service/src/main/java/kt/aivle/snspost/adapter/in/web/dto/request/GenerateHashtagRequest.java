package kt.aivle.snspost.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kt.aivle.snspost.domain.model.SnsPlatform;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GenerateHashtagRequest {

    @NotBlank(message = "게시물 제목은 필수입니다")
    private String postTitle;

    @NotBlank(message = "게시물 내용은 필수입니다")
    private String postContent;

    @NotEmpty(message = "사용자 키워드는 필수입니다")
    private List<String> userKeywords;

    @NotNull(message = "SNS 플랫폼은 필수입니다")
    private SnsPlatform snsPlatform;

    @NotBlank(message = "업종은 필수입니다")
    private String businessType;

    private String location;
} 