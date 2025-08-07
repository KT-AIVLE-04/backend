package kt.aivle.sns.domain.model;

public class YoutubeUpdateDetail {
    private String categoryId; // 카테고리 번호로 입력 (String)

    public YoutubeUpdateDetail() {};

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
