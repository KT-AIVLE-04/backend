package kt.aivle.sns.adapter.in.web.dto;

import lombok.Getter;

@Getter
public class YoutubeUpdateDetail {
    private String categoryId; // 카테고리 번호로 입력 (String)

    public YoutubeUpdateDetail() {};

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
