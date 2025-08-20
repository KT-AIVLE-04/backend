package kt.aivle.sns.adapter.in.web.dto;

public class PostUpdateRequest {
    private String postId; // 게시글 ID
    private Long storeId;
    private String title;
    private String description;
    private String[] tags;
    private Object detail; // SNS별 세부정보

    public PostUpdateRequest() {};

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public Object getDetail() {
        return detail;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }

    public Long getStoreId() {
        return storeId;
    }
}
