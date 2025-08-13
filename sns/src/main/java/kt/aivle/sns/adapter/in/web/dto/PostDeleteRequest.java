package kt.aivle.sns.adapter.in.web.dto;

public class PostDeleteRequest {
    private String postId;
    private Long storeId;

    public PostDeleteRequest() {};

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Long getStoreId() {
        return storeId;
    }
}
